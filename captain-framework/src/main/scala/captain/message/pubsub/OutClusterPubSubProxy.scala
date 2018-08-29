package captain.message.pubsub

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}
import captain.message.pubsub.OutClusterPubSubProtocol.{OutPublish, OutPublishResponse, OutSubscribe, OutSubscribeAck}

object OutClusterPubSubProxy {
  def props = Props(new OutClusterPubSubProxy)
}

class OutClusterPubSubProxy extends Actor with ActorLogging {

  private[this] val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = mediator ! Put(self)

  override def receive: Receive = {
    case pub: OutPublish[_] =>
      mediator ! Send(path = OutClusterPubSubProtocol.OUT_CLUSTER_PUBSUB_MEDIATOR_PATH, msg = pub, localAffinity = true)

    case sub: OutSubscribe =>
      mediator ! Send(path = OutClusterPubSubProtocol.OUT_CLUSTER_PUBSUB_MEDIATOR_PATH, msg = sub, localAffinity = true)

    case OutPublishResponse(pub, ref) =>
      ref ! pub.message

    case ack @ OutSubscribeAck(sub) =>
      sub.actorRef ! ack
  }
}
