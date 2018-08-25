package captain.model.pubsub

case class Topic private[captain] (path: List[String] = List.empty) {
  def /(end: String) = Topic((end :: path.reverse).reverse) // any better way?
  def /(postPath: Topic) = Topic(path ::: postPath.path)
}
