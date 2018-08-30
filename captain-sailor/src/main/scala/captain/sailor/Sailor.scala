package captain.sailor

import captain.sailor.router.{MicroService, Mqtt, StartRouter}
import captain.message.topic._

object Sailor extends App {
  StartRouter(
    Mqtt("test" / "topic") ~> MicroService("test" / "path")
  )
}
