package captain.sailor

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import captain.sailor.cluster.Coordinator

object Sailor {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("sailor-system")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val log = Logging(system.eventStream, this.getClass.getName)
    val coordinator = system.actorOf(Coordinator.props, "sailor-coordinator")
  }
}
