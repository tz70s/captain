package captain.message.pubsub

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import captain.message.pubsub.OutClusterPubSubProtocol.{OutPublish, OutSubscribe, OutSubscribeAck}
import captain.spec.FlatMultiNodeSpec

import captain.message._
import captain.message.pubsub.PubSubTestProtocol.DummyMessage

import scala.concurrent.duration._

class OutClusterPubSubMediatorMultiNodeSpec
    extends MultiNodeSpec(PubSubMultiNodeConfig)
    with FlatMultiNodeSpec
    with ImplicitSender {
  import captain.message.pubsub.PubSubMultiNodeConfig._

  override def initialParticipants: Int = 2

  def join(from: RoleName, to: RoleName): Unit = {
    runOn(from) {
      Cluster(system) join node(to).address
    }
    enterBarrier(s"${from.name}-join-${to.name}")
  }

  behavior of "OutCluster PubSub Mediator MultiNode"

  it should "make sure all members are joined" in within(15.seconds) {
    // join node1 and node2 manually.
    join(node1, node1)
    join(node2, node1)

    awaitAssert {
      Cluster(system).sendCurrentClusterState(testActor)
      expectMsgType[CurrentClusterState].members.size should equal(2)
    }

    runOn(node1) { OutClusterPubSubMediator() }
    runOn(node2) { OutClusterPubSubMediator() }

    enterBarrier("join-complete")
  }

  it should "be used to communicate correctly" in within(15.seconds) {
    runOn(node1) {
      val proxy = system.actorOf(OutClusterPubSubProxy.props)
      enterBarrier("subscribe-complete-1")
      // preparing message
      val pub = OutPublish("hello" / "world", DummyMessage("hello"))
      proxy ! pub
    }

    runOn(node2) {
      val proxy = system.actorOf(OutClusterPubSubProxy.props)
      val sub = OutSubscribe("hello" / "world", self)
      proxy ! sub
      expectMsg(5.seconds, OutSubscribeAck(sub))
      enterBarrier("subscribe-complete-1")
      expectMsg(5.seconds, DummyMessage("hello"))
    }

    enterBarrier("after-1")
  }

  it should "incremental subscribers and broadcast to them for subscription" in within(15.seconds) {
    runOn(node1) {
      val proxy = system.actorOf(OutClusterPubSubProxy.props)
      val sub = OutSubscribe("share" / "topic", self)
      proxy ! sub
      expectMsg(5.seconds, OutSubscribeAck(sub))
      enterBarrier("subscribe-complete-2")
      val dummy = DummyMessage("shared hello")
      proxy ! OutPublish("share" / "topic", dummy)
      expectMsg(5.seconds, dummy)
    }

    runOn(node2) {
      val proxy = system.actorOf(OutClusterPubSubProxy.props)
      val sub = OutSubscribe("share" / "topic", self)
      proxy ! sub
      expectMsg(5.seconds, OutSubscribeAck(sub))
      enterBarrier("subscribe-complete-2")
      expectMsg(5.seconds, DummyMessage("shared hello"))
    }

    enterBarrier("after-2")
  }
}

class OutClusterPubSubMediatorMultiNodeMultiJvmNode1 extends OutClusterPubSubMediatorMultiNodeSpec
class OutClusterPubSubMediatorMultiNodeMultiJvmNode2 extends OutClusterPubSubMediatorMultiNodeSpec
