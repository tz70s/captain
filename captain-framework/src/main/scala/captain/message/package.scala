package captain

package object message {
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
