package captain.message.pubsub

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import captain.message.Topic
import captain.message.pubsub.OutClusterPubSubProtocol.{OutPublish, OutSubscribe, OutSubscribeAck}

import scala.concurrent.duration._
import scala.util.Random

object OutClusterPubSubMediator {
  // Forcible factory to ensure actor name.
  def create(implicit system: ActorSystem): ActorRef =
    system.actorOf(props, OutClusterPubSubProtocol.OUT_CLUSTER_PUBSUB_MEDIATOR_NAME)

  private[this] def props = Props(new OutClusterPubSubMediator)
}

private[captain] class OutClusterPubSubMediator extends Actor with ActorLogging {

  private[this] val mediator = DistributedPubSub(context.system).mediator
  private[this] val replicator = DistributedData(context.system).replicator

  // This value may be dangerous in large scale nodes? We should make this configurable.
  private[this] val rwAllTimeout = 5.seconds
  private[this] val PubSubKey = ORMultiMapKey[Topic, ActorRef]("out-cluster-pubsub-mediator-or-multi-map")
  private[this] implicit val node = Cluster(context.system)

  override def preStart(): Unit = {
    super.preStart()
    mediator ! Put(self)
    replicator ! Subscribe(PubSubKey, self)
  }

  override def receive: Receive = {

    /// OutPublish: ddata read handling

    case pub @ OutPublish(topic, msg) =>
      // checkout registry value
      replicator ! Get(PubSubKey, ReadAll(rwAllTimeout), request = Some(pub))

    case get @ GetSuccess(PubSubKey, Some(pub: OutPublish[_])) =>
      val predicates = get.get(PubSubKey).get(pub.topic)
      // randomly pick up a predicate to go through.
      predicates
        .flatMap { l =>
          val rand = Random.nextInt() % l.size
          l.drop(rand).headOption
        }
        .foreach(ref => ref ! pub.message)

    // Ignoring get failure now ...

    /// OutSubscribe: ddata write handling

    case sub @ OutSubscribe(topic, ref) =>
      replicator ! Update(PubSubKey, ORMultiMap.empty[Topic, ActorRef], WriteAll(rwAllTimeout), request = Some(sub))(
        _.addBinding(topic, ref)
      )

    case update @ UpdateSuccess(PubSubKey, Some(sub: OutSubscribe)) =>
      sub.actorRef ! OutSubscribeAck(sub)
  }
}
