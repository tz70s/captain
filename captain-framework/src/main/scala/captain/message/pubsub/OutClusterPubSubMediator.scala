package captain.message.pubsub

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}
import captain.message.Topic
import captain.message.pubsub.OutClusterPubSubProtocol.{OutPublish, OutPublishResponse, OutSubscribe, OutSubscribeAck}

import scala.concurrent.duration._

private[captain] object OutClusterPubSubMediator {
  // Forcible factory to ensure actor name.
  // This mediator node should be singleton in a local actor system.
  // We'll have to force this creation being bounded.
  @volatile private[this] var mediator: Option[ActorRef] = None

  def apply()(implicit system: ActorSystem): Unit =
    mediator.orElse {
      mediator = Some(system.actorOf(props, OutClusterPubSubProtocol.OUT_CLUSTER_PUBSUB_MEDIATOR_NAME))
      mediator
    }

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
      replicator ! Get(PubSubKey, ReadAll(rwAllTimeout), request = Some((pub, sender())))

    case get @ GetSuccess(PubSubKey, Some((pub: OutPublish[_], proxy: ActorRef))) =>
      val predicates = get.get(PubSubKey).get(pub.topic)
      // Broadcast to all subscribers.
      predicates.foreach { set =>
        set.foreach { ref =>
          mediator ! Send(path = proxy.path.toStringWithoutAddress,
                          msg = OutPublishResponse(pub, ref),
                          localAffinity = true)
        }
      }

    // Ignoring get failure now ...

    /// OutSubscribe: ddata write handling

    case sub @ OutSubscribe(topic, ref) =>
      replicator ! Update(PubSubKey,
                          ORMultiMap.empty[Topic, ActorRef],
                          WriteAll(rwAllTimeout),
                          request = Some((sub, sender())))(
        _.addBinding(topic, ref)
      )

    case update @ UpdateSuccess(PubSubKey, Some((sub: OutSubscribe, proxy: ActorRef))) =>
      mediator ! Send(path = proxy.path.toStringWithoutAddress, msg = OutSubscribeAck(sub), localAffinity = true)
  }
}
