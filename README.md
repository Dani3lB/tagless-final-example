# tagless-final-example

This repository have been created to help me explain tagless final to my teammates.

## Script:

Monads are just a typeclass — basically, a way of describing a set of mathematical properties. They don't have anything inherently to do with side effects or I/O. It just so happens that you can model I/O as a monad, and that this is the default, built-in way to do I/O in Haskell.

What is a type class?
A way to reach polymorphism
What is polymorphism?
Access objects of different types through the same interface (pl java inheritance)

OOP approach consolidates data and related functiona in one place (class definition). Type classes: entites representing data are decoupled from entities responsible for implementation.

What does tagless final mean?
What is its advantages?
1. Create libraries not depending on concrete types
2. Least power principle