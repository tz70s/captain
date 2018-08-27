package captain.message.pubsub

import akka.NotUsed
import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{CurrentTopics, GetTopics}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.pattern.ask
import akka.util.Timeout
import captain.message.{MessageFlow, MessageProvider, Topic}

import scala.concurrent.Future
import scala.concurrent.duration._

object PubSubProvider extends MessageProvider {
  override def flowOf[T](topic: Topic, bufferSize: Int)(implicit system: ActorSystem): MessageFlow[T] =
    new PubSubMessageFlow[T](topic, bufferSize)
}

class PubSubMessageFlow[T] private[captain] (val topic: Topic, val bufferSize: Int)(implicit val system: ActorSystem)
    extends MessageFlow[T] {

  import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}

  private[this] val mediator = DistributedPubSub(system).mediator

  override def publish[T](message: T): Unit =
    mediator ! Publish(topic.toString, message)

  override def subscriber: Source[T, NotUsed] =
    Source
      .actorRef[T](bufferSize, OverflowStrategy.dropHead)
      .mapMaterializedValue { ref =>
        mediator ! Subscribe(topic.toString, ref)
        NotUsed
      }

  def hasAnySubscribers(): Future[Boolean] = {
    implicit val ec = system.dispatcher
    implicit val timeout = Timeout(5.seconds) // TODO: put this to a proper place.
    (mediator ? GetTopics)
      .map {
        case CurrentTopics(topics) => topics.contains(topic.toString)
      }
      .mapTo[Boolean]
  }
}
