package captain.message.pubsub

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.sharding.{ClusterSharding, ShardRegion}
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.{ImplicitSender, TestProbe}
import captain.message.pubsub.PubSubMultiNodeConfig.{node1, node2}
import captain.spec.FlatMultiNodeSpec
import captain.message._

import scala.concurrent.duration._

class EmbeddedPubSubMultiNodeSpec
    extends MultiNodeSpec(PubSubMultiNodeConfig)
    with FlatMultiNodeSpec
    with ImplicitSender {

  override def initialParticipants: Int = 2

  def join(from: RoleName, to: RoleName): Unit = {
    runOn(from) {
      Cluster(system) join node(to).address
      new EmbeddedPubSubShard()
    }
    enterBarrier(s"${from.name}-join-${to.name}")
  }

  behavior of "Embedded PubSub MultiNode"

  it should "make sure all members are joined" in {
    // join node1 and node2 manually.
    join(node1, node1)
    join(node2, node1)
    awaitAssert {
      Cluster(system).sendCurrentClusterState(testActor)
      expectMsgType[CurrentClusterState].members.size should equal(2)
    }
    enterBarrier("join-complete")
  }

  it should "return empty state when no sharded actors has started" in {
    awaitAssert {
      val probe = TestProbe()
      val region = ClusterSharding(system).shardRegion(OutClusterPubSubProtocol.PUB_SUB_SHARD_NAME)
      region.tell(ShardRegion.GetCurrentRegions, probe.ref)
      probe.expectMsgType[ShardRegion.CurrentRegions].regions.size should equal(0)
    }
  }

  it should "pipe back subscribe ack from shard" in within(15.seconds) {
    import OutClusterPubSubProtocol._

    runOn(node1) {
      enterBarrier("shard-created-1")
      val proxy = PubSubShard.proxy
      val sub = OutSubscribe("hello" / "world", self)
      proxy ! sub
      expectMsg(5.seconds, OutSubscribeAck(sub))
    }

    runOn(node2) {
      enterBarrier("shard-created-1")
    }

    enterBarrier("after-1")
  }
}

class EmbeddedPubSubMultiNodeMultiJvmNode1 extends EmbeddedPubSubMultiNodeSpec
class EmbeddedPubSubMultiNodeMultiJvmNode2 extends EmbeddedPubSubMultiNodeSpec
