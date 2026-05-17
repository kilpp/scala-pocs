package com.example

import scalaz.*
import Scalaz.*

object DataTypesDemo:

  def run(): Unit =
    println("=== Data Types ===")

    val just: Maybe[Int] = Maybe.just(42)
    val empty: Maybe[Int] = Maybe.empty[Int]
    println(s"Maybe.just -> $just  Maybe.empty -> $empty")
    println(s"Maybe getOrElse -> ${empty.getOrElse(0)}")

    val good: String \/ Int = 42.right[String]
    val bad: String \/ Int  = "boom".left[Int]
    println(s"Disjunction right -> $good  left -> $bad")
    val chained = for
      a <- good
      b <- 8.right[String]
    yield a + b
    println(s"Disjunction for-comp -> $chained")

    val nel: NonEmptyList[Int] = NonEmptyList(1, 2, 3, 4)
    println(s"NonEmptyList -> $nel  head -> ${nel.head}  size -> ${nel.size}")

    val il: IList[Int] = IList(10, 20, 30)
    println(s"IList -> $il  reverse -> ${il.reverse}")

    val these: Int \&/ String = \&/.Both(1, "one")
    println(s"These (Both) -> $these")

    val tree: Tree[Int] = 1.node(2.leaf, 3.node(4.leaf, 5.leaf))
    println(s"Tree.drawTree ->\n${tree.drawTree}")
