package captain.model.event

import java.util.UUID

import captain.model.event.EventBase.EventId

object EventBase {
  private[captain] case class EventId(id: UUID)

  def apply(): EventBase = new EventBase(EventId(UUID.randomUUID()))
}

class EventBase private[captain] (val id: EventId)
