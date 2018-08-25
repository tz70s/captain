package captain.sailor

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import captain.sailor.cluster.{Coordinator, Middleman}

import scala.util.{Failure, Success}

object Sailor {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("sailor-system")
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher

    val coordinator = system.actorOf(Coordinator.props, "sailor-coordinator")
    val middleman = system.actorOf(Middleman.props, "sailor-middleman")

    val log = Logging(system.eventStream, this.getClass.getName)

    val graph = Source(0 to 100)
      .toMat(Sink.fold(0)(_ + _))(Keep.right)

    graph.run().onComplete {
      case Success(t) =>
        log.info(s"result: $t")
      case Failure(e) =>
        log.error(s"error occurred, ${e.getMessage}")
    }
  }
}
