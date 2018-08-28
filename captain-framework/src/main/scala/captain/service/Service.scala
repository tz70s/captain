package captain.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import captain.message.MessageProtocol.ClusterRange
import captain.message.{MessagingService, Topic}

/**
 * Service trait is the core interface to implement a single microservice.
 *
 * User (developer) implements this Service trait with several reactions to specific event channel.
 */
trait Service extends MessagingService {

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  def actOf[Message](topic: Topic, bufferSize: Int, range: ClusterRange)(reaction: Message => Unit) =
    messaging.flowOf(topic, bufferSize, range).subscriber.map(message => reaction(message))
}
