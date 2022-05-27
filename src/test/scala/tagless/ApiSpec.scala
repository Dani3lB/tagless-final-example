package tagless

import model.Exceptions.UsernameIsTaken
import model.User
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ApiSpec extends AnyWordSpec with Matchers {

  trait Scope {
    implicit val db: Database[Either[Throwable, *]] = new InMemoryDatabaseInstance[Either[Throwable, *]]()
    val api: Api[Either[Throwable, *]] = new ApiInstance[Either[Throwable, *]]

    val username = "name"
    val user: User = User(username, 1000)
  }

  "#register" should {
    "add user" in new Scope {
      api.register(username) shouldBe Right(user)
    }

    "fail with error if user already exists" in new Scope {
      db.addUser(username)

      api.register(username) shouldBe Left(UsernameIsTaken())
    }
  }

}
