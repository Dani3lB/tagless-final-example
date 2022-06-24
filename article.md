# Tagless final - from a new perspective

## What is this article about?

This article is for scala developers who have heard about tagless final
but don't know if they should use it application development or not. Some concepts which should be familiar for you:
context bound, typeclass, 

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
is already done. Peters task is to implement a decrease money mechanism on the backend and handle when there is
not enough money to decrease from. Let's see Peters journey if the application is written without tagless final and with it.

### Without tagless final

```scala

```