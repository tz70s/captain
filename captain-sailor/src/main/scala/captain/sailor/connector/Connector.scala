package captain.sailor.connector

import akka.Done
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

trait Connector {
  def source: Source[ByteString, Future[Done]]
  def sink: Sink[ByteString, Future[Done]]
}
