package captain.service

import akka.actor.{ActorSystem, Terminated}
import akka.stream.ActorMaterializer
import captain.message.{ClusterRange, MessagingService, Topic}

import scala.concurrent.Future

trait ActorSystemInjection {
  implicit val system: ActorSystem
}

/**
 * This trait provide a default actor system impl.
 * This should be an option and let user define their desired actor system impl.
 * The only purpose is to let pubsub visibility sharable with all microservices, without negotiating protocols.
 * Note: We should deprecate this in the future.
 */
trait SharableClusterActorSystem extends ActorSystemInjection {
  // TODO: this trait should be along with pubsub messaging service.
  final private val SHARABLE_CLUSTER_ACTOR_SYSTEM = "clustered-actor-system"
  // final implicit lazy override val system = ActorSystem(SHARABLE_CLUSTER_ACTOR_SYSTEM)
}

/**
 * Service trait is the core interface to implement a single microservice.
 *
 * User (developer) implements this Service trait with several reactions to specific event channel.
 */
trait Service extends MessagingService {
  self: ActorSystemInjection =>

  implicit lazy val mat: ActorMaterializer = ActorMaterializer()

  final def actOf[Message](topic: Topic, bufferSize: Int, range: ClusterRange)(reaction: Message => Unit) =
    messaging.flowOf(topic, bufferSize, range).subscriber.map(message => reaction(message))

  /** Close out resources in this service. */
  final def terminate(): Future[Terminated] =
    system.terminate()
}
