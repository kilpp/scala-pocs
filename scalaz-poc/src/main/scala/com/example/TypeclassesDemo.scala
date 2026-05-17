package com.example

import scalaz.*
import Scalaz.*

object TypeclassesDemo:

  def run(): Unit =
    println("=== Typeclasses ===")

    val showInt: String  = Show[Int].shows(42)
    val showList: String = Show[List[Int]].shows(List(1, 2, 3))
    println(s"Show[Int] -> $showInt")
    println(s"Show[List[Int]] -> $showList")

    println(s"Equal[Int] 1 === 1 -> ${1 === 1}")
    println(s"Equal[Int] 1 =/= 2 -> ${1 =/= 2}")

    println(s"Order.max(3, 7) -> ${Order[Int].max(3, 7)}")
    println(s"Order.min('a','z') -> ${Order[Char].min('a', 'z')}")

    val sum  = Monoid[Int].zero |+| 1 |+| 2 |+| 3
    val strs = "hello, " |+| "world"
    val maps = Map("a" -> 1) |+| Map("a" -> 2, "b" -> 5)
    println(s"Monoid sum -> $sum")
    println(s"Monoid strings -> $strs")
    println(s"Monoid maps (combine values) -> $maps")

    val mapped: List[Int] = Functor[List].map(List(1, 2, 3))(_ * 10)
    println(s"Functor map -> $mapped")

    val ap: Option[Int] = (1.some |@| 2.some |@| 3.some)(_ + _ + _)
    println(s"Applicative |@| -> $ap")

    val flat: Option[Int] = for
      a <- 10.some
      b <- 32.some
    yield a + b
    println(s"Monad for-comp -> $flat")

    val folded: Int       = Foldable[List].foldMap(List(1, 2, 3, 4))(identity)
    val traversed         = List(1.some, 2.some, 3.some).sequence
    val traverseFiltered  = List(1, 2, 3).traverse(n => if n > 0 then n.some else none[Int])
    println(s"Foldable foldMap -> $folded")
    println(s"Traverse sequence -> $traversed")
    println(s"Traverse traverse -> $traverseFiltered")
