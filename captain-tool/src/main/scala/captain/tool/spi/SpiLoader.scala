package captain.tool.spi

import java.lang.ClassCastException

import com.typesafe.config.ConfigFactory

import scala.reflect.ClassTag

final case class SpiLoadingException(private val message: String = "", private val cause: Throwable = None.orNull)
    extends RuntimeException(message, cause)

trait Spi

object SpiLoader {

  private[this] val config = ConfigFactory.load()

  def get[S <: Spi: ClassTag]: S = {
    val className = lookup
    val clazz = Class.forName(className + "$")
    try {
      clazz.getField("MODULE$").get().asInstanceOf[S]
    } catch {
      case e: ClassCastException =>
        throw SpiLoadingException(s"spi loading failure: ${e.getMessage}", e.getCause)
    }
  }

  private[this] def lookup[S <: Spi: ClassTag]: String = {
    val provider = implicitly[ClassTag[S]].runtimeClass.getSimpleName
    config.getString("captain.spi." + provider)
  }
}
