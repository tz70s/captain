package captain.message.pubsub

import akka.NotUsed
import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import captain.message.{MessageFlow, MessageProvider, Topic}
import play.api.libs.json.OWrites

object PubSubProvider extends MessageProvider {
  override def flowOf[T](topic: Topic, bufferSize: Int)(implicit system: ActorSystem): MessageFlow[T] =
    new PubSubMessageFlow[T](topic, bufferSize)
}

class PubSubMessageFlow[T] private[captain] (val topic: Topic, val bufferSize: Int)(implicit val system: ActorSystem)
    extends MessageFlow[T] {

  import akka.cluster.pubsub.DistributedPubSubMediator.Publish

  private[this] val mediator = DistributedPubSub(system).mediator

  override def publish[U <: T: OWrites](message: U): Unit =
    mediator ! Publish(topic.toString, message)

  override def subscriber: Source[T, NotUsed] =
    Source
      .actorRef[T](bufferSize, OverflowStrategy.dropHead)
      .mapMaterializedValue { ref =>
        mediator ! Subscribe(topic.toString, ref)
        NotUsed
      }
}
