package captain.message.pubsub

import akka.actor.ActorSystem
import captain.message.MessageProtocol.{ClusterRange, InClusterRange, OutClusterRange}
import captain.message.{MessageFlow, MessageProvider, Topic}

object PubSubProvider extends MessageProvider {
  override def flowOf[T](topic: Topic, bufferSize: Int, range: ClusterRange)(
      implicit system: ActorSystem
  ): MessageFlow[T] =
    range match {
      case InClusterRange => new InClusterPubSub[T](topic, bufferSize)
      case OutClusterRange => new OutClusterPubSub[T](topic, bufferSize)
    }
}
