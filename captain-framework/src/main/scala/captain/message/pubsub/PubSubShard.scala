package captain.message.pubsub

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}

/**
 * Currently, this is useful to test OutClusterPubSub without spawning Sailor instance.
 * Not sure if this is useful to user, close out visibility now.
 *
 * Design points:
 *
 * A PubSubShard keep an event routing pool, to connect with pubsub clients, upstream connections and downstream connections.
 *
 * For PubSub client: similar model to distributed pubsub, using OutPublish and OutSubscribe message to achieve the same goal.
 */
private[captain] class EmbeddedPubSubShard(implicit system: ActorSystem) {
  import OutClusterPubSubProtocol._

  // Shard identifications

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg @ OutPublish(topic, _) => (topic.toString, msg)
    case msg @ OutSubscribe(topic, _) => (topic.toString, msg)
  }

  val numOfShards = 10

  val extractShardId: ShardRegion.ExtractShardId = {
    case OutPublish(topic, _) => (topic.hashCode() % numOfShards).toString
    case OutSubscribe(topic, _) => (topic.hashCode() % numOfShards).toString
  }

  ClusterSharding(system).start(
    typeName = PUB_SUB_SHARD_NAME,
    entityProps = PubSubShard.props,
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )
}

private[captain] object PubSubShard {

  /** Helper function to create a shard proxy, a.k.a. proxy. */
  def proxy(implicit system: ActorSystem): ActorRef =
    ClusterSharding(system).shardRegion(OutClusterPubSubProtocol.PUB_SUB_SHARD_NAME)

  def props = Props(new PubSubShard)
}

private[captain] class PubSubShard extends Actor with ActorLogging {
  import OutClusterPubSubProtocol._

  override def preStart(): Unit = {
    super.preStart()
    log.info("Start a pubsub shard ...")
  }

  override def receive: Receive = {
    case pub @ OutPublish(topic, message) =>
      log.info(s"receive publish $pub")

    case sub @ OutSubscribe(topic, actorRef) =>
      log.info(s"receive subscribe $sub")
      actorRef ! OutSubscribeAck(sub)

    case msg =>
      log.info(s"$msg")
  }
}
