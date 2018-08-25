package captain.sailor.cluster

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

object Coordinator {
  def props = Props(new Coordinator)
}

/** Coordinator is responsible for recording cluster states, and carefully make correct cluster routing decision. */
class Coordinator extends Actor with ActorLogging {

  private[this] val cluster = Cluster(context.system)

  override def preStart(): Unit =
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember], classOf[ReachableMember])

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info(s"member $member is up")

    case MemberJoined(member) =>
      log.info(s"new member $member is joined.")

    case MemberRemoved(member, previousStatus) =>
      log.info(s"member $member is removed, previous status : $previousStatus")

    case _: MemberEvent => // ignore.
  }
}
