package captain.sailor.cluster

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import captain.message._

class MiddlemanSpec()
    extends TestKit(ActorSystem("middleman-spec-system"))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  behavior of "Middleman"

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  it should "provide pub ack for publish" in {}

  it should "provide sub ack for subscriber" in {}
}
