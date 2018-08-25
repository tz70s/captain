package captain.message.pubsub

import akka.NotUsed
import akka.stream.scaladsl.Source
import captain.message.{MessageFlow, MessageProvider, Topic}
import play.api.libs.json.OWrites

object PubSubProvider extends MessageProvider {

  override def flowOf[T](topic: Topic): MessageFlow[T] = ???
}

class PubSubMessageFlow[T] extends MessageFlow[T] {
  override def publish[U <: T: OWrites](message: U): Unit = ???
  override def subscriber: Source[T, NotUsed] = ???
}
