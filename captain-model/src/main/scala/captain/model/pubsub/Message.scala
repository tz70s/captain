package captain.model.pubsub

import play.api.libs.json.{Json, OWrites}

private[captain] object PubSubProtocol {

  /** Determine whether messaging is in cluster or out cluster */
  sealed trait ClusterRange
  case object OutClusterRange extends ClusterRange
  case object InClusterRange extends ClusterRange

  case class Publish(topic: Topic, message: SerializedMessage)
  case class Subscribe(topic: Topic)

  case class PubAck(topic: Topic)
  case class SubAck(topic: Topic)
}

/**
 * We've adopted a more strict model to make sure serialization between network peers work.
 * Therefore, all message should be Json serializable. (may further support Protobuf if needed.)
 *
 * The message should satisfy OWrites in Play Json.
 *
 * TODO: We should provide some way to validate on the reverse side.
 */
case class Message[T: OWrites](serde: T) {
  def toJson = Json.toJson(serde)
  override def toString: String = toJson.toString()
  def toSerializedMessage = SerializedMessage(toString)
}

private[captain] case class SerializedMessage(payload: String)
