package captain.message

import captain.tool.spi.{Spi, SpiLoader}

trait MessageProvider extends Spi {
  def flowOf[T](topic: Topic): MessageFlow[T]
}

trait MessagingService {
  protected val messaging = SpiLoader.get[MessageProvider]
}
