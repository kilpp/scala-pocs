package com.example

import cats.{Functor, Monad, Monoid, Show}
import cats.syntax.all.*

object TypeclassesDemo:
  def run(): Unit =
    println("--- TypeclassesDemo ---")

    // Functor: map over any F[_]
    val opt: Option[Int] = Some(21)
    val lst: List[Int]   = List(1, 2, 3)
    println(s"Functor[Option].map: ${Functor[Option].map(opt)(_ * 2)}")
    println(s"Functor[List].map:   ${lst.fmap(_ + 10)}")

    // Monad: flatMap + for-comprehension across effects uniformly
    val combined: Option[Int] =
      for
        a <- Option(10)
        b <- Option(32)
      yield a + b
    println(s"Monad[Option] sum:   $combined")

    // Generic function abstracting over any Monad
    def pair[F[_]: Monad, A](fa: F[A], fb: F[A]): F[(A, A)] =
      for
        a <- fa
        b <- fb
      yield (a, b)
    println(s"pair on Option: ${pair(Option(1), Option(2))}")
    println(s"pair on List:   ${pair(List(1, 2), List(10, 20))}")

    // Semigroup / Monoid: combine values
    val combinedStrings = "foo" |+| "bar"
    val combinedMaps    = Map("a" -> 1, "b" -> 2) |+| Map("b" -> 3, "c" -> 4)
    println(s"Semigroup String: $combinedStrings")
    println(s"Monoid Map[Int]:  $combinedMaps")
    println(s"Monoid[Int].empty = ${Monoid[Int].empty}")

    // Show: type-safe toString
    given Show[(String, Int)] = (t: (String, Int)) => s"${t._1}=${t._2}"
    println(s"Show tuple: ${("answer", 42).show}")
    println()
