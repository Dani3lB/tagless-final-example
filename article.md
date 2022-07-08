# Tagless final - from a new perspective

## What is this article about?

This article is for scala developers who have heard about tagless final
but don't know if they should use it application development or not. Some concepts which should be familiar for you:
scala, context bound, typeclass, and tagless final itself

Tagless final is a very controversial topic in case of application development, and I don't want to convince anyone.
My intention is to tell you about my opinion and why would I use it in every scala program.

## Advantages of tagless final

Tagless final is nothing new but programming to an interface. This comes with a few advantages. The most popular two:
1. Create libraries not depending on concrete types
2. Easily change the concrete effect you are working with (for example from scala Future to cats IO)

These are trivial and agreed by everyone as a good thing, but some say it is not worth it to use tagless final
in application development, because the first is for libs and the second happens rarely.

I agree with them, I wouldn't use it for the second one only neither. But there is another less known advantage, the
**least power principle**.

## Least power principle
Originally, the least power principle (or rule of least power) was created for programming languages:
"Use the least powerful language suitable for expressing information, constraints or programs on the World Wide Web."
([source](https://www.w3.org/2001/tag/doc/leastPower.html))

I think the same is true for every part of a program, including every layer and class. I would say something like:
"A class should **not be able** to do more than it is intended".
Maybe you say, how could a class do more than it is intended? Instead of lengthy explanations let me jump right into
an example which I can explain afterwards.

## In practice
Look at the case of Programmer Peter. His company wants to create a money handler application, and the registration
is already done. Peter's task is to implement a decrease money mechanism on the backend and handle when there is
not enough money to decrease from. Let's see Peters journey if the application is written without tagless final and with it.

### Without tagless final

In this case, the application uses cats IO as an effect type, and that is hard-coded everywhere. Peter checks the project
structure: database layer; API layer; and the endpoint. He decides to start from the bottom and checks the database class:

```scala
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
}
```

It is an inmemory database, which could be pure, but the team used IO, so they can replace the implementation to work with
an actual database later easier.

Peter uses TDD, so he starts with a test:

```scala
trait Scope {
    val db = new Database()
    
    val username = "name"
    val user: User = User(username, 1000)
}

"#decreaseMoney" should {
  "decrease amount" in new Scope {
    db.addUser(username).unsafeRunSync()

    db.decreaseMoney(username, 100).unsafeRunSync() shouldBe user.copy(money = user.money-100)
  }
}
```

And the implementation:

```scala
def decreaseMoney(name: String, amount: Int): IO[User] = {
  IO.apply {
    val idx = users.indexWhere(_.name == name)
    val user = users(idx)
    val modifiedUser = user.copy(money = user.money - amount)
    users.update(idx, modifiedUser)
    modifiedUser
  }
}
```

Great! So easy so far. Peter, as a good programmer, wants to cover edge cases as well. Let's see the next test and implementation:

```scala
"return error if user does not exist" in new Scope {
  db.decreaseMoney("doesnotexist", 100).attempt.unsafeRunSync() shouldBe Left(UserDoesNotExist())
}
```

```scala
def decreaseMoney(name: String, amount: Int): IO[User] = {
  IO.defer {
    val idx = users.indexWhere(_.name == name)
    if (idx == -1) {
      IO.raiseError(new UserDoesNotExist)
    } else {
      val user = users(idx)
      val modifiedUser = user.copy(money = user.money - amount)
      users.update(idx, modifiedUser)
      IO.pure(modifiedUser)
    }
  }
}
```

And the last one:

```scala
"return error if there is not enough money to decrease" in new Scope {
  db.addUser(username)

  db.decreaseMoney(username, 999).unsafeRunSync()
  db.decreaseMoney(username, 100).attempt.unsafeRunSync() shouldBe Left(NonSufficientFunds())
}
```

Whoops! Working with side effects is not easy, and Peter have made a mistake. The db.addUser is called but not ran, so this
test will fail with UserDoesNotExist. Fortunately it is easy to understand why and fix it, but he has to pay attention to this
in the future. The fixed test and the implementation:

```scala
"return error if there is not enough money to decrease" in new Scope {
  db.addUser(username).unsafeRunSync()

  db.decreaseMoney(username, 999).unsafeRunSync()
  db.decreaseMoney(username, 100).attempt.unsafeRunSync() shouldBe Left(NonSufficientFunds())
}
```

```scala
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
```

Great, he finished the database. Let's see the API layer:


TODO:
- IO api part
- Tagless final part
- Summary