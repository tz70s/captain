package captain.sailor.router

import captain.message.Topic

/** Construct DSL for generating routing table. */
sealed trait RoutingType {
  def ~>(dest: RoutingType) = Route(this, dest)
}
case class Mqtt(topic: Topic) extends RoutingType
case class HttpWs(topic: Topic) extends RoutingType
case class MicroService(topic: Topic) extends RoutingType

case class Route(source: RoutingType, dest: RoutingType)

case class RoutingTable(routes: Route*) {
  private[this] val internalMap = routes.groupBy(_.source)
  def get(source: RoutingType): Option[Seq[Route]] = internalMap.get(source)
}
