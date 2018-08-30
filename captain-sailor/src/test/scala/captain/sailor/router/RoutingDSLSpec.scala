package captain.sailor.router

import org.scalatest.{FlatSpec, Matchers}
import captain.message.topic._

class RoutingDSLSpec extends FlatSpec with Matchers {

  behavior of "Routing DSL"

  it should "generate a correct routing table via fluent DSL" in {
    val table = RoutingTable(
      Mqtt("test" / "mqtt" / "topic-0") ~> HttpWs("test" / "http" / "path-0"),
      Mqtt("test" / "mqtt" / "topic-1") ~> MicroService("test" / "microservice" / "path-0")
    )

    table.get(Mqtt("test" / "mqtt" / "topic-0")) should be(
      Some(Seq(Route(Mqtt("test" / "mqtt" / "topic-0"), HttpWs("test" / "http" / "path-0"))))
    )
  }
}
