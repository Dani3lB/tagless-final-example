package tagless

import model.Exceptions.{NonSufficientFunds, UserDoesNotExist, UsernameIsTaken}
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

  "#decreaseMoney" should {
    "decrease amount" in new Scope {
      db.addUser(username)

      api.decreaseMoney(username, 100) shouldBe Right(user.copy(money = user.money-100))
    }

    "return error if user does not exist" in new Scope {
      db.addUser(username)

      api.decreaseMoney("doesnotexist", 100) shouldBe Left(UserDoesNotExist())
    }

    "return error if there is not enough money to decrease" in new Scope {
      db.addUser(username)

      api.decreaseMoney(username, 999)
      api.decreaseMoney(username, 100) shouldBe Left(NonSufficientFunds())
    }
  }

}
