package captain.service

import akka.actor.ActorSystem
import captain.message.topic._
import captain.service.TestService.TestMessage
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

object TestService {
  case class TestMessage(name: String)
  implicit val writes = Json.writes[TestMessage]
  implicit val reads = Json.reads[TestMessage]
}

class TestService()(implicit system: ActorSystem) extends CallService[TestMessage, TestMessage] with ServiceLogging {
  import TestService._

  log.info(s"Initialize a TestService")

  private[this] def logMethod = CallSync[TestMessage, TestMessage] { msg =>
    println(s"receive $msg")
    log.info(s"receive $msg")
    CallSuccess(msg)
  }

  override val descriptor = Descriptor(PubSubCallContext("test" / "topic") -> logMethod)
}

class ServiceSpec extends FlatSpec with Matchers {

  behavior of "Service"

  it should "normally initialized" in {
    import TestService._
    Service.withCall[TestMessage, TestMessage, TestService]()
    Service.terminate()
  }
}
