# Tagless final - from a new perspective

## What is this article about?

This article is for scala developers who have heard about tagless final
but don't know if they should use it in application development or not. Some concepts which should be familiar to you:
scala, context bound, type class, and the tagless final itself.

The tagless final pattern is a very controversial topic in the case of application development, and I don't want to convince anyone.
I intend to tell you about my opinion and why would I use it in every scala program.

## Advantages of tagless final

Tagless final is nothing new but programming to an interface. This comes with a few advantages. The most popular two:
1. Create libraries not depending on concrete types
2. Easily change the concrete effect you are working with (for example from scala Future to cats IO)

These are trivial and agreed by everyone as a good thing, but some say it is not worth it to use tagless final
in application development because the first is for libs and the second happens rarely.

I agree with them, I wouldn't use it for the second one only either. But there is another less known advantage, the
**least power principle**.

## Least power principle
Originally, the least power principle (or rule of least power) was created for programming languages:
"Use the least powerful language suitable for expressing information, constraints or programs on the World Wide Web."
([source](https://www.w3.org/2001/tag/doc/leastPower.html))

I think the same is true for every part of a program, including every layer and class. I would say something like:
"A class should **not be able** to do more than it is intended".
Maybe you say, how could a class do more than it is intended? Instead of lengthy explanations let me jump right into
an example which I can explain afterward.

## In practice
Look at the case of Programmer Peter. His company wants to create a money handler application, and the registration
is already done. Peter's task is to implement a money decreaser mechanism on the backend and handle when there is
not enough money to decrease from. Let's see Peter's journey if the application is written without tagless final and with it.

### Without tagless final

In this case, the application uses cats IO as an effect type, and that is hard-coded everywhere. Peter checks the project
structure: database layer; API layer; and the endpoint. He decides to start from the bottom which is the database class:

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

Whoops! Working with side effects is not easy, and Peter has made a mistake. The db.addUser is called but not run, so this
test will fail with UserDoesNotExist. Fortunately, it is easy to understand why and fix it, but he has to pay attention to this
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

```scala
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
}
```

It should be really easy to extend it, let's try:

```scala
"#decreaseMoney" should {
  "decrease money" in new Scope {
    db.addUser(username).unsafeRunSync()

    api.decreaseMoney(username, 100).unsafeRunSync() shouldBe user.copy(money = user.money-100)
  }
}
```

```scala
def decreaseMoney(name: String, amount: Int): IO[User] = database.decreaseMoney(name, amount)
```

Well, that was easy. Peter handled every edge case in the database class, so now he just has to call it.
This went quite well. Can it become better, with tagless final? Let's see.

### With tagless final

As in the case with IO, Peter checks the database class first.

```scala
trait Database[F[_]] {
  def getUser(name: String): F[Option[User]]
  def addUser(name: String): F[User]
  def decreaseMoney(name: String, amount: Int): F[User]
}

object Database {
  def apply[F[_]](implicit database: Database[F]): Database[F] = database
}

class InMemoryDatabaseInstance[F[_]: Applicative] extends Database[F] {
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
}
```

(Note: in `addUser` the pure around the append is not that elegant, but for the example and for the consistency with
the IO case I'll leave it like this for now.)

Looks simple. Peter starts with a test without hesitation:

```scala
trait Scope {
  val db: Database[Either[Throwable, *]] = new InMemoryDatabaseInstance[Either[Throwable, *]]()

  val username = "name"
  val user: User = User(username, 1000)
}

"#decreaseMoney" should {
  "decrease amount" in new Scope {
    db.addUser(username)

    db.decreaseMoney(username, 100) shouldBe Right(user.copy(money = user.money-100))
  }
}
```

For testing, he uses Either as an effect type, so the tests will be pure. He doesn't have to pay attention to unsafeRunSync
in this case. The implementation:

```scala
override def decreaseMoney(name: String, amount: Int): F[User] = {
  Applicative[F].pure{
    val idx = users.indexWhere(_.name == name)
    val user = users(idx)
    val modifiedUser = user.copy(money = user.money - amount)
    users.update(idx, modifiedUser)
    modifiedUser
  }
}
```

It's quite similar to the IO case so far. Let's see the error cases! Oh wait... we can't. Peter would like to implement
the case when the user is not found, but he can't raise an error because of the Applicative context bound. Because of this, he
checks the API layer:

```scala
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
}
```

Aha! The error handling is in the API layer, which can be seen right away from the MonadThrow context bound.
Peter continues with a test, which is the same happy path as in the InMemoryDatabaseInstance class. I think you get that
and the corresponding implementation by now, so let's jump to the error case:

```scala
"return error if user does not exist" in new Scope {
  db.addUser(username)

  api.decreaseMoney("doesnotexist", 100) shouldBe Left(UserDoesNotExist())
}
```

The implementation:

```scala
override def decreaseMoney(name: String, amount: Int): F[User] = {
  for {
    maybeExistingUser <- Database[F].getUser(name)
    newUser <- maybeExistingUser match {
      case None => MonadThrow[F].raiseError(UserDoesNotExist())
      case Some(user) => Database[F].decreaseMoney(name, amount)
    }
  } yield newUser
}
```

And the case when there is not enough balance:

```scala
"return error if there is not enough money to decrease" in new Scope {
  db.addUser(username)

  api.decreaseMoney(username, 999)
  api.decreaseMoney(username, 100) shouldBe Left(NonSufficientFunds())
}
```

```scala
override def decreaseMoney(name: String, amount: Int): F[User] = {
  for {
    maybeExistingUser <- Database[F].getUser(name)
    newUser <- maybeExistingUser match {
      case None => MonadThrow[F].raiseError(UserDoesNotExist())
      case Some(user) => if(user.money - amount >= 0) Database[F].decreaseMoney(name, amount) else MonadThrow[F].raiseError(NonSufficientFunds())
    }
  } yield newUser
}
```

This is the completed task with tagless final. Is it better? I think yes. Here are my thoughts:

## Summary

As you can see, in the second, tagless final solution the functions inside the layers are consistent. Error handling
only happens in the API layer, so the database layer has less ownership, and less *power*. This is the least power principle,
and it can be forced with this approach. Well... can it be really forced? Actually no, Peter could have changed the context
bound to MonadThrow and done the very same thing as in the IO case. It depends on team culture and experience.

But it helps testing without a doubt, right? Well, no. Of course, working with effects can lead to strange issues, but
for example, Peter could have used for comprehension in the IO test cases to not have that issue. Again, it depends
on taste and experience.

Why is it good then? I don't think this approach is always good, but it's good for me. I think teams should decide
if the added complexity is worth it or not. For example, new colleagues may have a hard time understanding this kind of
programming to interfaces approach, especially if they are new to scala too. However, in a well-established team, I think
tagless final can improve the quality of the application.