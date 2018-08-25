package captain.message

import org.scalatest.{FlatSpec, Matchers}

class MessageFlowSpec extends FlatSpec with Matchers {

  behavior of "Topic"

  it should "construct from string and path implicitly" in {
    val topic = "hello" / "world"
    topic shouldBe Topic(List("hello", "world"))
  }

  it should "concatenate in correct order" in {
    val first = "this" / "is"
    val second = "a" / "topic" / "test"
    val merge = first / second
    merge shouldBe Topic(List("this", "is", "a", "topic", "test"))
  }
}
