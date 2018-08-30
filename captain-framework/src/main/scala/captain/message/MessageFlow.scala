package captain.message

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}

trait MessageFlow[T] {
  def publish(message: T)
  def publisher: Sink[T, NotUsed]
  def subscriber: Source[T, NotUsed]
}
