package captain.service

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class MockService extends Service with SharableClusterActorSystem {}

class ServiceSpec extends FlatSpec with Matchers {

  behavior of "Service"

  it should "normally initialized" in {
    val mockService = new MockService
    Await.ready(mockService.terminate(), 30.seconds)
  }
}
