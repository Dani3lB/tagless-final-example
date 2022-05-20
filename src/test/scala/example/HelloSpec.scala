package example

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HelloSpec extends AnyWordSpec with Matchers {
  "The Hello object" should {
    "say hello" in {
      Hello.greeting shouldEqual "hello"
    }
  }
}
