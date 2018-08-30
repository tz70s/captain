package captain.service

import akka.actor.{ActorSystem, Terminated}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import play.api.libs.json.{OWrites, Reads}

import scala.concurrent.Future
import scala.reflect.ClassTag

trait ActorSystemInjection {
  implicit val system: ActorSystem

  /** Close out resources in this service. */
  final def terminate(): Future[Terminated] =
    system.terminate()
}

/**
 * This trait provide a default actor system impl.
 * This should be an option and let user define their desired actor system impl.
 * The only purpose is to let pubsub visibility sharable with all microservices, without negotiating protocols.
 * Note: We should deprecate this in the future.
 */
class SharableClusterActorSystem extends ActorSystemInjection {
  // TODO: this trait should be along with pubsub messaging service.
  final private val SHARABLE_CLUSTER_ACTOR_SYSTEM = "clustered-actor-system"
  final implicit lazy override val system = ActorSystem(SHARABLE_CLUSTER_ACTOR_SYSTEM)
}

trait ServiceLogging {
  @volatile private var _log: Option[LoggingAdapter] = None
  def log(implicit system: ActorSystem) = _log.getOrElse { _log = Some(Logging(system, this.getClass)); _log.get }
}

object Service {

  private[this] lazy val sharableClusterActorSystem = new SharableClusterActorSystem

  def withCall[M: Reads, R: OWrites, C <: CallService[M, R]: ClassTag](
      injection: ActorSystemInjection = sharableClusterActorSystem
  ): Unit = {
    implicit val system = sharableClusterActorSystem.system
    implicit val mat = ActorMaterializer()
    val ref = implicitly[ClassTag[C]].runtimeClass.getConstructor(classOf[ActorSystem]).newInstance(system)
    val casted = ref.asInstanceOf[C]
    casted.start[M, R]
  }

  def terminate(): Future[Terminated] = sharableClusterActorSystem.terminate()
}
