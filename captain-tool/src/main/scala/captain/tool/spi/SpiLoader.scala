package captain.tool.spi

import com.typesafe.config.ConfigFactory

import scala.reflect.ClassTag

trait Spi

object SpiLoader {

  private[this] val config = ConfigFactory.load()

  def get[S <: Spi: ClassTag]: S = {
    val className = lookup
    val clazz = Class.forName(className + "$")
    clazz.getField("MODULE$").get().asInstanceOf[S]
  }

  private[this] def lookup[S <: Spi: ClassTag]: String = {
    val provider = implicitly[ClassTag[S]].runtimeClass.getSimpleName
    config.getString("captain.spi." + provider)
  }
}
