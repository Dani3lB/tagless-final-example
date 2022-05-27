# tagless-final-example

This repository has been created to help me explain tagless final to my teammates.

## Material:

**Monads are just** a typeclass — basically, **a way of describing a set of** mathematical **properties**. They don't have anything
inherently to do with side effects or I/O. It just so happens that you can model I/O as a monad, and that this is the
default, built-in way to do I/O in Haskell.

A typeclass is a way to reach polymorphism.

Polymorphism is when you access objects of different types through the same interface (for example inheritance).

OOP approach consolidates data and related functions in one place (class definition). Type classes: entities
representing data are decoupled from entities responsible for implementation.

```scala
import cats.Monad // the type itself
import cats.syntax.MonadSyntax // the extension methods
```

>What is an extension method? <br/>
Extension Method allows us to add a new method to an existing class:
>- Without modifying it or adding code
>- Without extending it or creating a new derived type
>- Without recompiling the class<br/><br/>
>For example: TemplateDBIO[F].loadById(templateId).**adaptError**(???)

What does tagless final mean? <br />
See 3. reference (~30 minutes). TF ~ Programming to interfaces

What are its advantages?
1. Create libraries not depending on concrete types
2. Easily change the concrete effect you are working with
3. Least power principle

## Script

- goal: why should you use tagless final?
- explain the material
- show syntaxes of the 2 implementation
- case of Barkacs Bela
  - task: implement a decrease money mechanism and handle when there is not enough money to decrease from

References
- https://medium.com/@olxc/type-classes-explained-a9767f64ed2c
- https://www.infragistics.com/community/blogs/b/dhananjay_kumar/posts/what-is-the-extension-method-in-c
- https://blog.rockthejvm.com/tagless-final/