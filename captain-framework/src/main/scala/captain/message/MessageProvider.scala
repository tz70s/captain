package captain.message

import akka.actor.ActorSystem
import captain.message.topic.Topic
import captain.tool.spi.{Spi, SpiLoader}

private[captain] trait MessageProvider extends Spi {
  def flowOf[T](topic: Topic, bufferSize: Int, range: ClusterRange)(implicit system: ActorSystem): MessageFlow[T]
}

trait MessagingService {
  protected final val messaging = SpiLoader.get[MessageProvider]
}
