package captain.tool.spi

import org.scalatest.{FlatSpec, Matchers}

class SpiLoaderSpec extends FlatSpec with Matchers {

  behavior of "SpiLoader"

  it should "correctly load spi from typesafe config" in {
    val provider = SpiLoader.get[TestMockProvider]
    provider.info should be(MockProvider.INFO)
  }
}
