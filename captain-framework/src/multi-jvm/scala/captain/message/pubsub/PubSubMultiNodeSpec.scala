package captain.message.pubsub

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.remote.testconductor.RoleName
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.ImplicitSender
import captain.spec.FlatMultiNodeSpec
import captain.message._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.Await

object PubSubMultiNodeConfig extends MultiNodeConfig {
  // There's no special distinguished under messaging.
  val node1 = role("node1")
  val node2 = role("node2")

  commonConfig(ConfigFactory.parseString("""akka.actor.provider = "cluster""""))

  // Barriers in test suite.
  val STARTUP = "startup"
  val DEPLOYED = "deployed"
  val FINISHED = "finished"
}

object PubSubTestProtocol {
  case class DummyMessage(content: String)
}

class PubSubMultiNodeSpec extends MultiNodeSpec(PubSubMultiNodeConfig) with FlatMultiNodeSpec with ImplicitSender {
  import PubSubMultiNodeConfig._
  import PubSubTestProtocol._

  implicit val materializer = ActorMaterializer()

  override def initialParticipants: Int = roles.size

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
    enterBarrier(STARTUP)
  }

  behavior of "PubSub MultiNode"

  it should "receive end-to-end messaging" in within(15.seconds) {
    // publisher
    runOn(node1) {
      val pubSubFlow = new PubSubMessageFlow[DummyMessage]("test" / "pub" / "sub", 10)
      awaitCond(Await.result(pubSubFlow.hasAnySubscribers(), 5.seconds))
      enterBarrier(DEPLOYED)
      pubSubFlow.publish(DummyMessage("message-1"))
      pubSubFlow.publish(DummyMessage("message-2"))
    }

    // subscriber
    runOn(node2) {
      val pubSubFlow = new PubSubMessageFlow[DummyMessage]("test" / "pub" / "sub", 10)
      val subscriber = pubSubFlow.subscriber
      val probe = subscriber.runWith(TestSink.probe[DummyMessage]).request(2)

      enterBarrier(DEPLOYED)
      probe.expectNext(DummyMessage("message-1"), DummyMessage("message-2"))
    }

    enterBarrier(FINISHED)
  }
}

class PubSubMultiNodeMultiJvmNode1 extends PubSubMultiNodeSpec
class PubSubMultiNodeMultiJvmNode2 extends PubSubMultiNodeSpec
