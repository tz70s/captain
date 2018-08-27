package captain.message.pubsub

private[captain] object PubSubProtocol {

  /** Determine whether message is in cluster or out cluster */
  sealed trait ClusterRange
  case object OutClusterRange extends ClusterRange
  case object InClusterRange extends ClusterRange
}
