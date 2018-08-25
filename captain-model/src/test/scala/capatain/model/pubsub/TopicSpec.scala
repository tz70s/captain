package capatain.model.pubsub

import org.scalatest.{FlatSpec, Matchers}

import captain.model.pubsub._

class TopicSpec extends FlatSpec with Matchers {

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
