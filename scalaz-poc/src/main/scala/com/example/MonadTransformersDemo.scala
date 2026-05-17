package com.example

import scalaz.*
import Scalaz.*
import scalaz.effect.IO

object MonadTransformersDemo:

  // OptionT lets us combine IO + Option without ugly nesting.
  private def lookup(id: Int): OptionT[IO, String] =
    OptionT[IO, String](IO {
      Map(1 -> "alpha", 2 -> "beta").get(id)
    })

  private val both: OptionT[IO, String] = for
    a <- lookup(1)
    b <- lookup(2)
  yield s"$a + $b"

  private val missing: OptionT[IO, String] = for
    a <- lookup(1)
    b <- lookup(99) // None -> short-circuits
  yield s"$a + $b"

  // EitherT layers a \/ over IO.
  private def safeDiv(n: Int, d: Int): EitherT[String, IO, Int] =
    EitherT[String, IO, Int](IO {
      if d == 0 then "div by zero".left else (n / d).right
    })

  private val program: EitherT[String, IO, Int] = for
    a <- safeDiv(100, 5)
    b <- safeDiv(a, 2)
  yield b

  def run(): Unit =
    println("=== Monad Transformers ===")
    println(s"OptionT[IO, *] both    -> ${both.run.unsafePerformIO()}")
    println(s"OptionT[IO, *] missing -> ${missing.run.unsafePerformIO()}")
    println(s"EitherT program        -> ${program.run.unsafePerformIO()}")
    println(s"EitherT failure        -> ${safeDiv(1, 0).run.unsafePerformIO()}")
