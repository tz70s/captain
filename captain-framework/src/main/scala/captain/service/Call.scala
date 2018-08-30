package captain.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import captain.message.topic.Topic
import captain.message.{ClusterRange, InClusterRange, MessagingService}
import captain.service.CallService._
import play.api.libs.json.{OWrites, Reads}

private[captain] object CallService {
  val DEFAULT_CLUSTER_RANGE = InClusterRange
  val DEFAULT_PUBSUB_BUFFER_SIZE = 10
}

sealed trait CallResult[T]

case class CallSuccess[T](result: T) extends CallResult[T]
case class CallFailure[T](ex: Throwable) extends CallResult[T]

sealed trait CallContext

case class PubSubCallContext(topic: Topic,
                             bufferSize: Int = DEFAULT_PUBSUB_BUFFER_SIZE,
                             range: ClusterRange = DEFAULT_CLUSTER_RANGE)
    extends CallContext

trait CallService[Message, Result] extends MessagingService {
  type Call[M, R] = M => CallResult[R]

  final protected def callOf[M <: Message: Reads, R <: Result: OWrites](callContext: CallContext)(
      callMethod: Call[M, R]
  )(implicit system: ActorSystem, mat: ActorMaterializer) =
    callContext match {
      case PubSubCallContext(topic, bufferSize, range) =>
        messaging.flowOf(topic, bufferSize, range).subscriber.map(msg => callMethod(msg)).toMat(Sink.seq)(Keep.right)
    }

  def descriptor: Descriptor[Message, Result]

  private[captain] final def start(implicit system: ActorSystem,
                                   mat: ActorMaterializer,
                                   reads: Reads[Message],
                                   writes: OWrites[Result]): Unit =
    for ((ctx, call) <- descriptor.internal) { callOf(ctx)(call) }
}

case class Descriptor[Message, Result](calls: (CallContext, Message => CallResult[Result])*) {
  private[captain] val internal = calls.toMap
}
