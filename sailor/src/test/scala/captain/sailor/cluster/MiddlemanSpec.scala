package captain.sailor.cluster

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import captain.model.pubsub.PubSubProtocol.{PubAck, Publish, SubAck, Subscribe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import captain.model.pubsub._

class MiddlemanSpec()
    extends TestKit(ActorSystem("middleman-spec-system"))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  behavior of "Middleman"

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  it should "provide pub ack for publish" in {
    val middleman = system.actorOf(Middleman.props)
    middleman ! Publish("hello" / "world", SerializedMessage("hello"))
    expectMsg(PubAck("hello" / "world"))
  }

  it should "provide sub ack for subscribe" in {
    val middleman = system.actorOf(Middleman.props)
    middleman ! Subscribe("hello" / "world")
    expectMsg(SubAck("hello" / "world"))
  }
}
