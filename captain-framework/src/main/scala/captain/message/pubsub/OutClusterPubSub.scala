package captain.message.pubsub

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Sink, Source}
import captain.message.{MessageFlow, Topic}

object OutClusterPubSubProtocol {
  val PUB_SUB_SHARD_NAME = "PubSubShardName"

  case class OutPublish[T](topic: Topic, message: T)
  case class OutSubscribe(topic: Topic, actorRef: ActorRef)
  case class OutSubscribeAck(outSub: OutSubscribe)
}

/**
 * The OutClusterPubSub is mediated by event hub, a.k.a Sailor instances.
 * In each sailor instances contain several shards to proxy connections from/to upstream (edge clusters or cloud) and protocol bridges.
 * For user perspective, this is encapsulated however.
 */
class OutClusterPubSub[T] private[captain] (topic: Topic, bufferSize: Int)(implicit system: ActorSystem)
    extends MessageFlow[T] {

  override def publish(message: T): Unit = ???

  override def publisher: Sink[T, NotUsed] = ???

  override def subscriber: Source[T, NotUsed] = ???
}
