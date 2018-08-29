package captain.sailor.router

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer

object StartRouter {
  def apply(routes: Route*) = new Router(RoutingTable(routes: _*))
}

private[captain] class Router(table: RoutingTable) {
  // Start all services.
  implicit val system = ActorSystem("sailor-system")
  implicit val materializer = ActorMaterializer()
  private[this] val log = Logging(system.eventStream, this.getClass.getName)
  log.info(s"Start router service in Sailor instance.")
}
