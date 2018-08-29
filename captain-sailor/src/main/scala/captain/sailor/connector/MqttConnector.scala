package captain.sailor.connector

import akka.stream.ActorMaterializer
import akka.{Done, NotUsed}
import akka.stream.alpakka.mqtt.scaladsl.{MqttSink, MqttSource}
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSourceSettings}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import captain.message.Topic
import captain.sailor.connector.MqttConnector.{BrokerAddress, MqttClientId}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.Future

object MqttConnector {
  case class BrokerAddress(host: String, port: Int) {
    override def toString: String = s"$host:$port"
  }
  case class MqttClientId(id: String) {
    override def toString: String = id
  }

  def apply(brokerAddress: BrokerAddress, clientId: MqttClientId, topic: Topic, bufferSize: Int)(
      implicit mat: ActorMaterializer
  ) =
    new MqttConnector(brokerAddress, clientId, topic, bufferSize)

}

class MqttConnector(brokerAddress: BrokerAddress, clientId: MqttClientId, topic: Topic, bufferSize: Int)(
    implicit mat: ActorMaterializer
) extends Connector {
  private[this] val connectionSettings =
    MqttConnectionSettings(s"tcp://$brokerAddress", clientId.toString, new MemoryPersistence)

  private[this] val sourceSettings = connectionSettings.withClientId(clientId = s"$clientId/source")
  private[this] val sinkSettings = connectionSettings.withClientId(clientId = s"$clientId/sink")

  override def source: Source[ByteString, Future[Done]] = {
    val settings = MqttSourceSettings(sourceSettings, Map(topic.toString -> MqttQoS.atMostOnce))
    MqttSource.atMostOnce(settings, bufferSize = bufferSize).map(_.payload)
  }

  override def sink: Sink[ByteString, Future[Done]] =
    Flow[ByteString]
      .map(
        bytes => MqttMessage(topic.toString, payload = bytes, qos = Some(MqttQoS.atMostOnce))
      )
      .toMat(MqttSink(sinkSettings, MqttQoS.atMostOnce))(Keep.right)
}
