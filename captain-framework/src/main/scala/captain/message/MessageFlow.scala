package captain.message

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}

final case class Topic private[captain] (path: List[String] = List.empty) {
  def /(end: String) = Topic((end :: path.reverse).reverse) // any better way?
  def /(postPath: Topic) = Topic(path ::: postPath.path)

  override def toString: String = path.mkString("-")
}

trait MessageFlow[T] {
  def publish(message: T)
  def publisher: Sink[T, NotUsed]
  def subscriber: Source[T, NotUsed]
}
