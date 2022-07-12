package tagless

import cats.Applicative
import model.User

import scala.collection.mutable

trait Database[F[_]] {
  def getUser(name: String): F[Option[User]]
  def addUser(name: String): F[User]
  def decreaseMoney(name: String, amount: Int): F[User]
}

object Database {
  def apply[F[_]](implicit database: Database[F]): Database[F] = database
} // => Database[F].getUser

class InMemoryDatabaseInstance[F[_]: Applicative] extends Database[F] { // F has an associated Applicative (implicit a: Applicative[F])
  private val startingMoney = 1000
  private val users: mutable.ArrayDeque[User] = mutable.ArrayDeque.empty

  override def getUser(name: String): F[Option[User]] = Applicative[F].pure(users.find(_.name == name))

  override def addUser(name: String): F[User] = {
    Applicative[F].pure {
      val user = User(name, startingMoney)
      users.append(user)
      user
    }
  }

  override def decreaseMoney(name: String, amount: Int): F[User] = {
    Applicative[F].pure{
      val idx = users.indexWhere(_.name == name)
      val user = users(idx)
      val modifiedUser = user.copy(money = user.money - amount)
      users.update(idx, modifiedUser)
      modifiedUser
    }
  }
}
