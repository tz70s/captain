package captain.message

import akka.NotUsed
import akka.stream.scaladsl.Source
import play.api.libs.json.OWrites

case class Topic private[captain] (path: List[String] = List.empty) {
  def /(end: String) = Topic((end :: path.reverse).reverse) // any better way?
  def /(postPath: Topic) = Topic(path ::: postPath.path)
}

trait MessageFlow[T] {
  def publish[U <: T: OWrites](message: U)
  def subscriber: Source[T, NotUsed]
}
