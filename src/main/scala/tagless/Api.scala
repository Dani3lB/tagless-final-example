package tagless

import cats.MonadThrow
import cats.syntax.flatMap._
import cats.syntax.functor._
import model.Exceptions.{NonSufficientFunds, UserDoesNotExist, UsernameIsTaken}
import model.User

trait Api[F[_]] {
  def register(name: String): F[User]
  def decreaseMoney(name: String, amount: Int): F[User]
}

object Api {
  def apply[F[_]](implicit api: Api[F]): Api[F] = api
}

class ApiInstance[F[_]: MonadThrow: Database] extends Api[F] {
  override def register(name: String): F[User] = {
    for {
      maybeExistingUser <- Database[F].getUser(name)
      newUser <- maybeExistingUser match {
        case None => Database[F].addUser(name)
        case Some(_) => MonadThrow[F].raiseError(UsernameIsTaken())
      }
    } yield newUser
  }

  override def decreaseMoney(name: String, amount: Int): F[User] = {
    for {
      maybeExistingUser <- Database[F].getUser(name)
      newUser <- maybeExistingUser match {
        case None => MonadThrow[F].raiseError(UserDoesNotExist())
        case Some(user) => if(user.money - amount >= 0) Database[F].decreaseMoney(name, amount) else MonadThrow[F].raiseError(NonSufficientFunds())
      }
    } yield newUser
  }
}
