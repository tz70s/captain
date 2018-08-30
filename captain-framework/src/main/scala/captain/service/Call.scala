package captain.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import captain.message.topic.Topic
import captain.message.{ClusterRange, InClusterRange, MessagingService}
import captain.service.CallService._
import play.api.libs.json.{OWrites, Reads}

import scala.concurrent.Future

private[captain] object CallService {
  val DEFAULT_CLUSTER_RANGE = InClusterRange
  val DEFAULT_PUBSUB_BUFFER_SIZE = 10
}

private[captain] sealed trait CallResult[T]

private[captain] sealed trait CallSyncResult[T] extends CallResult[T]
case class CallSuccess[T](result: T) extends CallSyncResult[T]
case class CallFailure[T](ex: Throwable) extends CallSyncResult[T]

private[captain] sealed trait CallAsyncResult[T] extends CallResult[T]
case class CallAsyncSuccess[T](result: Future[T]) extends CallAsyncResult[T]
case class CallAsyncFailure[T](ex: Future[Throwable]) extends CallAsyncResult[T]

private[captain] sealed trait CallContext

case class PubSubCallContext(topic: Topic,
                             bufferSize: Int = DEFAULT_PUBSUB_BUFFER_SIZE,
                             range: ClusterRange = DEFAULT_CLUSTER_RANGE)
    extends CallContext

private[captain] sealed trait Call[M, R, +C <: CallResult[R]] {
  val call: M => C
}

object CallSync {
  def apply[M, R](call: M => CallSyncResult[R]) = new CallSync[M, R](call)
}
object CallAsync {
  def apply[M, R](call: M => CallAsyncResult[R]) = new CallAsync[M, R](call)
}

private[captain] class CallSync[M, R](override val call: M => CallSyncResult[R]) extends Call[M, R, CallSyncResult[R]]
private[captain] class CallAsync[M, R](override val call: M => CallAsyncResult[R])
    extends Call[M, R, CallAsyncResult[R]]

trait CallService[Message, Result] extends MessagingService {

  val descriptor: Descriptor[Message, Result]

  // FIXME: There's a type erasure occurred, would like to avoid this, but how?
  private[captain] final def start[M <: Message: Reads, R <: Result: OWrites](implicit system: ActorSystem,
                                                                              mat: ActorMaterializer): Unit =
    for ((ctx: CallContext, call: Call[M, R, CallResult[R]] @unchecked) <- descriptor.internal) {
      callOf[M, R](ctx)(call)
    }

  private[captain] final def callOf[M: Reads, R: OWrites](callContext: CallContext)(
      callMethod: Call[M, R, CallResult[R]]
  )(implicit system: ActorSystem, mat: ActorMaterializer) =
    callMethod match {
      case syncCall: CallSync[M, R] => syncCallOf(callContext)(syncCall)
      case asyncCall: CallAsync[M, R] => asyncCallOf(callContext)(asyncCall)
    }

  private[captain] final def syncCallOf[M: Reads, R: OWrites](
      callContext: CallContext
  )(callMethod: CallSync[M, R])(implicit system: ActorSystem) =
    callContext match {
      case PubSubCallContext(topic, bufferSize, range) =>
        messaging
          .flowOf(topic, bufferSize, range)
          .subscriber
          .map(msg => callMethod.call(msg))
          .toMat(Sink.seq)(Keep.right)
    }

  private[captain] final def asyncCallOf[M: Reads, R: OWrites](
      callContext: CallContext
  )(callMethod: CallAsync[M, R])(implicit system: ActorSystem) =
    callContext match {
      case PubSubCallContext(topic, bufferSize, range) =>
        messaging
          .flowOf(topic, bufferSize, range)
          .subscriber
          .map(msg => callMethod.call(msg))
          .toMat(Sink.seq)(Keep.right)
    }
}

case class Descriptor[Message, Result](
    calls: (CallContext, Call[Message, Result, CallResult[Result]])*
) {
  private[captain] val internal = calls.toMap
}
