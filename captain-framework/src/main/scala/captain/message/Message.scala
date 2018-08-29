package captain.message

import play.api.libs.json.{Json, OWrites}

/**
 * We've adopted a more strict model to make sure serialization between network peers work.
 * Therefore, all message should be Json serializable. (may further support Protobuf if needed.)
 *
 * The message should satisfy OWrites in Play Json.
 *
 * TODO: We should provide some way to validate on the reverse side.
 */
final case class Message[T: OWrites](serde: T) {
  def toJson = Json.toJson(serde)
  override def toString: String = toJson.toString()
  def toSerializedMessage = SerializedMessage(toString)
}

private[captain] case class SerializedMessage(payload: String)

/** Determine whether message is in cluster or out cluster */
sealed trait ClusterRange
case object OutClusterRange extends ClusterRange
case object InClusterRange extends ClusterRange
