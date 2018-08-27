package captain.spec

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

/** Mixin test helpers with multi-node test functionalities */
trait FlatMultiNodeSpec extends MultiNodeSpecCallbacks with FlatSpecLike with Matchers with BeforeAndAfterAll {
  override def beforeAll(): Unit = {
    super.beforeAll()
    multiNodeSpecBeforeAll()
  }

  override def afterAll(): Unit = {
    multiNodeSpecAfterAll()
    super.afterAll()
  }
}
