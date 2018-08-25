package captain.message

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

object MessageSpec {
  case class TestObject(number: Int, id: String)
  implicit def testWrites = Json.writes[TestObject]
  implicit def testReads = Json.reads[TestObject]
}

class MessageSpec extends FlatSpec with Matchers {

  behavior of "MessageSpec"

  it should "correctly serde from writable case class" in {
    import MessageSpec._
    val testObject = TestObject(5, "test")
    val message = Message(testObject)
    message.toJson shouldBe Json.obj("number" -> 5, "id" -> "test")
  }
}
