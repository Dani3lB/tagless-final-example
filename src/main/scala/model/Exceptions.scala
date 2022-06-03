package model

object Exceptions {
  final case class UsernameIsTaken() extends Exception
  final case class UserDoesNotExist() extends Exception
  final case class NonSufficientFunds() extends Exception
}
