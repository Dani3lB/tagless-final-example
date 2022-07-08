package io

import cats.effect.IO
import model.Exceptions.{NonSufficientFunds, UserDoesNotExist}
import model.User

import scala.collection.mutable

class Database {
  private val startingMoney = 1000
  private val users: mutable.ArrayDeque[User] = mutable.ArrayDeque.empty

  def getUser(name: String): IO[Option[User]] = IO.apply(users.find(_.name == name))

  def addUser(name: String): IO[User] = {
    IO.apply {
      val user = User(name, startingMoney)
      users.append(user)
      user
    }
  }

  def decreaseMoney(name: String, amount: Int): IO[User] = {
    IO.defer {
      val idx = users.indexWhere(_.name == name)
      if (idx == -1) {
        IO.raiseError(new UserDoesNotExist)
      } else {
        val user = users(idx)
        if (user.money - amount < 0) {
          IO.raiseError(new NonSufficientFunds)
        } else {
          val modifiedUser = user.copy(money = user.money - amount)
          users.update(idx, modifiedUser)
          IO.pure(modifiedUser)
        }
      }
    }
  }
}
