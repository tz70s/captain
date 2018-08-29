package captain.sailor

import captain.sailor.router.{MicroService, Mqtt, StartRouter}
import captain.message._

object Sailor extends App {
  StartRouter(
    Mqtt("test" / "topic") ~> MicroService("test" / "path")
  )
}
