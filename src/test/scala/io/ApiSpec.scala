package io

import cats.effect.unsafe.IORuntime
import model.Exceptions.UsernameIsTaken
import model.User
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ApiSpec extends AnyWordSpec with Matchers {

  implicit val runtime: IORuntime = IORuntime.global

  trait Scope {
    implicit val db: Database = new Database()
    val api = new Api()

    val username = "name"
    val user: User = User(username, 1000)
  }

  "#register" should {
    "add user" in new Scope {
      api.register(username).unsafeRunSync() shouldBe user
    }

    "fail with error if user already exists" in new Scope {
      db.addUser(username).unsafeRunSync()

      api.register(username).attempt.unsafeRunSync() shouldBe Left(UsernameIsTaken())
    }
  }

  "#decreaseMoney" should {
    "decrease money" in new Scope {
      db.addUser(username).unsafeRunSync()

      api.decreaseMoney(username, 100).unsafeRunSync() shouldBe user.copy(money = user.money-100)
    }
  }
}
