package captain.message.pubsub

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import captain.spec.FlatMultiNodeSpec

class OutClusterPubSubMultiNodeSpec
    extends MultiNodeSpec(PubSubMultiNodeConfig)
    with FlatMultiNodeSpec
    with ImplicitSender {

  override def initialParticipants: Int = 3

  behavior of "OutCluster PubSub MultiNode"

}
