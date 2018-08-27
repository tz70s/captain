package captain.message

import akka.actor.ActorSystem
import captain.tool.spi.{Spi, SpiLoader}

trait MessageProvider extends Spi {
  def flowOf[T](topic: Topic, bufferSize: Int)(implicit system: ActorSystem): MessageFlow[T]
}

trait MessagingService {
  protected val messaging = SpiLoader.get[MessageProvider]
}
