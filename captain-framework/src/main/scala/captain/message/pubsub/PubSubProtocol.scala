package captain.message.pubsub

import captain.message.Topic

private[captain] object PubSubProtocol {

  /** Determine whether message is in cluster or out cluster */
  sealed trait ClusterRange
  case object OutClusterRange extends ClusterRange
  case object InClusterRange extends ClusterRange

  case class Publish(topic: Topic, message: SerializedMessage)
  case class Subscribe(topic: Topic)

  case class PubAck(topic: Topic)
  case class SubAck(topic: Topic)

}

private[captain] case class SerializedMessage(payload: String)
