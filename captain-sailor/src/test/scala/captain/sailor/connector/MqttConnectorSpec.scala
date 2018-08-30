package captain.sailor.connector

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.TestKit
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.scalatest.concurrent.ScalaFutures
import captain.message.topic._

import scala.concurrent.Await
import scala.concurrent.duration._

class MqttConnectorSpec
    extends TestKit(ActorSystem("mqtt-connector-spec-system"))
    with FlatSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  import MqttConnector._

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  implicit val mat = ActorMaterializer()

  behavior of "Mqtt Connector"

  it should "communicate with pubsub" in {
    val dummy = ByteString("hello-0")

    val connector = MqttConnector(BrokerAddress("localhost", 1883), MqttClientId("test-client-0"), "test" / "topic", 10)
    val (subscribed, message) = connector.source.take(1).toMat(Sink.head)(Keep.both).run()

    Await.ready(subscribed, 5.seconds)
    Source.single(dummy).runWith(connector.sink)

    message.futureValue should be(dummy)
  }

  it should "pipe with multiple messages" in {
    val dummies = List(ByteString("hello-0"), ByteString("hello-1"), ByteString("hello-2"))

    val connector = MqttConnector(BrokerAddress("localhost", 1883), MqttClientId("test-client-0"), "test" / "topic", 10)
    val (subscribed, messages) = connector.source.take(dummies.size).toMat(Sink.seq)(Keep.both).run()

    Await.ready(subscribed, 5.seconds)

    Source(dummies).runWith(connector.sink)
    messages.futureValue should be(dummies)
  }
}
