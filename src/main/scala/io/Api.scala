package io

import cats.effect.IO
import model.Exceptions.UsernameIsTaken
import model.User

class Api(implicit database: Database) {
  def register(name: String): IO[User] = {
    for {
      maybeExistingUser <- database.getUser(name)
      newUser <- maybeExistingUser match {
        case None => database.addUser(name)
        case Some(_) => IO.raiseError(UsernameIsTaken())
      }
    } yield newUser
  }

  def decreaseMoney(name: String, amount: Int): IO[User] = ???

}
