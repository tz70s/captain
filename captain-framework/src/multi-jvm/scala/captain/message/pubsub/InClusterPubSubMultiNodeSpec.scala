package captain.message.pubsub

import akka.cluster.Cluster
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.ImplicitSender
import captain.spec.FlatMultiNodeSpec
import captain.message._

import scala.concurrent.duration._
import scala.concurrent.Await

class InClusterPubSubMultiNodeSpec extends MultiNodeSpec(PubSubMultiNodeConfig) with FlatMultiNodeSpec {
  import PubSubMultiNodeConfig._
  import PubSubTestProtocol._

  implicit val materializer = ActorMaterializer()

  override def initialParticipants: Int = 2

  def join(from: RoleName, to: RoleName) = {
    runOn(from) {
      Cluster(system) join node(to).address
    }
    enterBarrier(s"${from.name}-join-${to.name}")
  }

  override def atStartup(): Unit = {
    // join node1 and node2 manually.
    join(node1, node1)
    join(node2, node1)
    enterBarrier("join-complete")
  }

  behavior of "InCluster PubSub MultiNode"

  it should "receive end-to-end messaging" in within(15.seconds) {
    // publisher
    runOn(node1) {
      val pubSubFlow = new InClusterPubSub[DummyMessage]("test" / "pub" / "sub", 10)
      awaitCond(Await.result(pubSubFlow.hasAnySubscribers(), 5.seconds))
      enterBarrier("pubsub-create-1")
      pubSubFlow.publish(DummyMessage("message-1"))
      pubSubFlow.publish(DummyMessage("message-2"))
    }

    // subscriber
    runOn(node2) {
      val pubSubFlow = new InClusterPubSub[DummyMessage]("test" / "pub" / "sub", 10)
      val subscriber = pubSubFlow.subscriber
      val probe = subscriber.runWith(TestSink.probe[DummyMessage]).request(2)

      enterBarrier("pubsub-create-1")
      probe.expectNext(DummyMessage("message-1"), DummyMessage("message-2"))
    }

    enterBarrier("after-1")
  }

  it should "work with stream-based messaging" in within(15.seconds) {
    // publisher
    runOn(node1) {
      val pubSub = new InClusterPubSub[DummyMessage]("test" / "pub" / "sub", 10)
      awaitCond(Await.result(pubSub.hasAnySubscribers(), 5.seconds))
      enterBarrier("pubsub-create-2")
      val publisher = pubSub.publisher
      Source(List(DummyMessage("message-1"), DummyMessage("message-2"))).runWith(publisher)
    }

    // subscriber
    runOn(node2) {
      val pubSub = new InClusterPubSub[DummyMessage]("test" / "pub" / "sub", 10)
      val subscriber = pubSub.subscriber
      val probe = subscriber.runWith(TestSink.probe[DummyMessage]).request(2)

      enterBarrier("pubsub-create-2")
      probe.expectNext(DummyMessage("message-1"), DummyMessage("message-2"))
    }

    enterBarrier("after-2")
  }
}

class InClusterPubSubMultiNodeMultiJvmNode1 extends InClusterPubSubMultiNodeSpec
class InClusterPubSubMultiNodeMultiJvmNode2 extends InClusterPubSubMultiNodeSpec
