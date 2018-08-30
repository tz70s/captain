package example

import akka.actor.ActorSystem
import captain.service._
import captain.message.topic._
import play.api.libs.json.Json

case class IoTMessage(vendor: String, value: Int)
case object IoTMessage {
  implicit val writes = Json.writes[IoTMessage]
  implicit val reads = Json.reads[IoTMessage]
}

class MiniService(implicit system: ActorSystem) extends CallService[IoTMessage, IoTMessage] with ServiceLogging {

  log.info(s"Start a MiniService example.")

  private[this] def logIoTData(message: IoTMessage) = {
    log.info(s"receive message $message")
    CallSuccess(message)
  }

  override def descriptor: Descriptor[IoTMessage, IoTMessage] = Descriptor(
    PubSubCallContext("IoT" / "Data" / "Channel") -> logIoTData
  )
}

object MiniService extends App {
  import IoTMessage._
  Service.withCall[IoTMessage, IoTMessage, MiniService]()
}
