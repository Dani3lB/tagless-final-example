package tagless

import model.User
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DatabaseSpec extends AnyWordSpec with Matchers {

  trait Scope {
    val db: Database[Either[Throwable, *]] = new InMemoryDatabaseInstance[Either[Throwable, *]]()

    val username = "name"
    val user: User = User(username, 1000)
  }

  "#addUser" should {
    "add user to the db" in new Scope {
      db.addUser(username)

      db.getUser(username) shouldBe Right(Some(user))
    }

    "return the added user" in new Scope {
      db.addUser(username) shouldBe Right(user)
    }
  }

}
