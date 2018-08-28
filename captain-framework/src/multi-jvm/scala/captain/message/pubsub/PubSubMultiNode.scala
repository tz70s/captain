package captain.message.pubsub

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object PubSubMultiNodeConfig extends MultiNodeConfig {
  // There's no special distinguished under messaging.
  val node1 = role("node1")
  val node2 = role("node2")
  val node3 = role("node3")

  commonConfig(ConfigFactory.parseString(s"""
    akka.loglevel = "INFO"
    akka.actor.provider = "cluster"
    """))
}

object PubSubTestProtocol {
  case class DummyMessage(content: String)
}
