package io

import cats.effect.unsafe.IORuntime
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
      db.addUser(username)

      db.getUser(username).unsafeRunSync() shouldBe Some(user)
    }

    "return the added user" in new Scope {
      db.addUser(username).unsafeRunSync() shouldBe user
    }
  }

}