package captain.message

package object topic {
  final case class Topic private[captain] (path: List[String] = List.empty) {
    def /(end: String) = Topic((end :: path.reverse).reverse) // any better way?
    def /(postPath: Topic) = Topic(path ::: postPath.path)

    override def toString: String = path.mkString("-")
  }

  implicit def stringToPath(topicString: String) =
    if (topicString.length != 0) {
      var path = topicString.split("/")
      path = if (path(0) == "") path.drop(0) else path
      path = if (path.length != 0 && path(path.length - 1) == "") path.drop(path.length - 1) else path
      Topic(path.toList)
    } else {
      Topic()
    }
}
