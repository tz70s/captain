package captain.tool.spi

trait TestMockProvider extends Spi {
  def info = "this-should-be-override"
}

object MockProvider extends TestMockProvider {
  val INFO = "mock-provider-is-called"
  override def info: String = INFO
}
