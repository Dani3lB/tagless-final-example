package io

import cats.effect.unsafe.IORuntime
import model.Exceptions.{NonSufficientFunds, UserDoesNotExist}
import model.User
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DatabaseSpec extends AnyWordSpec with Matchers {

  implicit val runtime: IORuntime = IORuntime.global

  trait Scope {
    val db = new Database()

    val username = "name"
    val user: User = User(username, 1000)
  }

  "#addUser" should {
    "add user to the db" in new Scope {
      db.addUser(username).unsafeRunSync()

      db.getUser(username).unsafeRunSync() shouldBe Some(user)
    }

    "return the added user" in new Scope {
      db.addUser(username).unsafeRunSync() shouldBe user
    }
  }

  "#decreaseMoney" should {
    "decrease amount" in new Scope {
      db.addUser(username).unsafeRunSync()

      db.decreaseMoney(username, 100).unsafeRunSync() shouldBe user.copy(money = user.money-100)
    }

    "return error if user does not exist" in new Scope {
      db.addUser(username).unsafeRunSync()

      db.decreaseMoney("doesnotexist", 100).attempt.unsafeRunSync() shouldBe Left(UserDoesNotExist())
    }

    "return error if there is not enough money to decrease" in new Scope {
      db.addUser(username).unsafeRunSync()

      db.decreaseMoney(username, 999).unsafeRunSync()
      db.decreaseMoney(username, 100).attempt.unsafeRunSync() shouldBe Left(NonSufficientFunds())
    }
  }

}