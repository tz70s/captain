package captain.message.pubsub

import akka.NotUsed
import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{CurrentTopics, GetTopics}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import akka.pattern.ask
import captain.message.MessageFlow
import captain.message.topic.Topic

import scala.concurrent.Future
import scala.concurrent.duration._

private[captain] class InClusterPubSub[T](val topic: Topic, val bufferSize: Int)(implicit val system: ActorSystem)
    extends MessageFlow[T] {

  import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}

  private[this] val mediator = DistributedPubSub(system).mediator

  private[this] val hasAnySubscribersTimeout = Timeout(5.seconds)

  override def publish(message: T): Unit =
    mediator ! Publish(topic.toString, message)

  override def publisher: Sink[T, NotUsed] =
    Sink.foreach[T](msg => mediator ! Publish(topic.toString, msg)).mapMaterializedValue(_ => NotUsed)

  override def subscriber: Source[T, NotUsed] =
    Source
      .actorRef[T](bufferSize, OverflowStrategy.dropHead)
      .mapMaterializedValue { ref =>
        mediator ! Subscribe(topic.toString, ref)
        NotUsed
      }

  def hasAnySubscribers(): Future[Boolean] = {
    implicit val ec = system.dispatcher
    (mediator ? GetTopics)(hasAnySubscribersTimeout)
      .map {
        case CurrentTopics(topics) => topics.contains(topic.toString)
      }
      .mapTo[Boolean]
  }
}
