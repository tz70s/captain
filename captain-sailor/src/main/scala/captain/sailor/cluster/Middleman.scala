package captain.sailor.cluster

import akka.actor.{Actor, ActorLogging, Props}
import captain.message.pubsub.PubSubProtocol.{PubAck, Publish, SubAck, Subscribe}

object Middleman {
  def props = Props(new Middleman)
}

class Middleman extends Actor with ActorLogging {

  override def receive: Receive = {
    case Publish(topic, message) =>
      log.info(s"receive payload $message, from $topic")
      sender() ! PubAck(topic)

    case Subscribe(topic) =>
      log.info(s"receive subscriber message, to $topic")
      sender() ! SubAck(topic)
  }
}
