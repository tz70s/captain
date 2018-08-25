package captain.model.event

import captain.model.event.EventBase
import org.scalatest.{FlatSpec, Matchers}

class EventSpec extends FlatSpec with Matchers {

  behavior of "EventBase"

  it should "automatically generate event id" in {
    val event = EventBase()
    event.id should not be null
  }
}
