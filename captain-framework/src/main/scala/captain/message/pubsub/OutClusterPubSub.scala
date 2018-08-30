package captain.message.pubsub

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import captain.message.topic.Topic
import captain.message.MessageFlow

private[captain] object OutClusterPubSubProtocol {
  val PUB_SUB_SHARD_NAME = "PubSubShardName"

  val OUT_CLUSTER_PUBSUB_MEDIATOR_NAME = "out-cluster-pubsub-mediator"
  val OUT_CLUSTER_PUBSUB_MEDIATOR_PATH = s"/user/$OUT_CLUSTER_PUBSUB_MEDIATOR_NAME"

  case class OutPublish[T](topic: Topic, message: T)
  case class OutSubscribe(topic: Topic, actorRef: ActorRef)
  case class OutSubscribeAck(outSub: OutSubscribe)

  case class OutPublishResponse[T](publish: OutPublish[T], actorRef: ActorRef)
}

/**
 * The OutClusterPubSub is mediated by event hub, a.k.a Sailor instances.
 * In each sailor instances contain several shards to proxy connections from/to upstream (edge clusters or cloud) and protocol bridges.
 * For user perspective, this is encapsulated however.
 */
private[captain] class OutClusterPubSub[T] private[captain] (topic: Topic, bufferSize: Int)(
    implicit system: ActorSystem
) extends MessageFlow[T] {

  import OutClusterPubSubProtocol._

  private[this] val proxy = system.actorOf(OutClusterPubSubProxy.props)

  override def publish(message: T): Unit = proxy ! OutPublish(topic, message)

  override def publisher: Sink[T, NotUsed] =
    Sink.foreach[T](msg => proxy ! OutPublish(topic, msg)).mapMaterializedValue(_ => NotUsed)

  override def subscriber: Source[T, NotUsed] =
    Source.actorRef(bufferSize, OverflowStrategy.dropHead).mapMaterializedValue { ref =>
      proxy ! OutSubscribe(topic, ref)
      NotUsed
    }
}
