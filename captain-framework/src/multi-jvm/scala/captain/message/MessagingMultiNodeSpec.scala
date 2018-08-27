package captain.message

import akka.actor.{Actor, Props}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import captain.spec.FlatMultiNodeSpec

import scala.concurrent.duration._

// p2p messaging based on distributed pubsub tests

object MessagingMultiNodeConfig extends MultiNodeConfig {
  // There's no special distinguished under messaging.
  val node1 = role("node1")
  val node2 = role("node2")

  // Barriers in test suite.
  val STARTUP = "startup"
  val DEPLOYED = "deployed"
  val FINISHED = "finished"
}

object TestSample {
  class Echo extends Actor {
    override def receive: Receive = {
      case msg => sender() ! msg
    }
  }
}

/**
 * Write coordinated multi node tests for messaging infra.
 * For more detail, please refer to: https://doc.akka.io/docs/akka/2.5/multi-node-testing.html
 */
class MessagingMultiNodeSpec
    extends MultiNodeSpec(MessagingMultiNodeConfig)
    with FlatMultiNodeSpec
    with ImplicitSender {

  import MessagingMultiNodeConfig._

  override def initialParticipants: Int = roles.size

  behavior of "Messaging MultiNode"

  it should "wait for all nodes to enter a barrier" in {
    enterBarrier(STARTUP)
  }

  it should "send and receive from remote actor" in {
    runOn(node1) {
      enterBarrier(DEPLOYED)
      val echoActor = system.actorSelection(node(node2) / "user" / "echo")
      echoActor ! "hello"
      expectMsg(5.seconds, "hello")
    }

    runOn(node2) {
      system.actorOf(Props[TestSample.Echo], "echo")
      enterBarrier(DEPLOYED)
    }

    enterBarrier(FINISHED)
  }
}

// In order to use multi-jvm tests, should follow up naming conventions that plugin can search for these.
// Refer to: https://doc.akka.io/docs/akka/2.5/multi-jvm-testing.html#creating-application-tests
class MessagingMultiNodeMultiJvmNode1 extends MessagingMultiNodeSpec
class MessagingMultiNodeMultiJvmNode2 extends MessagingMultiNodeSpec
