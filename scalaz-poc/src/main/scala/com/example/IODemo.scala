package com.example

import scalaz.*
import Scalaz.*
import scalaz.effect.IO

object IODemo:

  private def putLn(s: String): IO[Unit] = IO(println(s))
  private val readLine: IO[String]       = IO("scalaz") // pretend stdin

  private val program: IO[Int] = for
    _    <- putLn("[IO] starting")
    name <- readLine
    _    <- putLn(s"[IO] hello, $name")
    n    <- IO(40 + 2)
    _    <- putLn(s"[IO] computed $n")
  yield n

  // Traverse lifts a function returning IO over a list, then sequences.
  private val traversed: IO[List[Int]] =
    List(1, 2, 3).traverse(n => IO(n * n))

  def run(): Unit =
    println("=== Effects (scalaz-effect IO) ===")
    val res = program.unsafePerformIO()
    println(s"IO result    -> $res")
    println(s"traverse IO  -> ${traversed.unsafePerformIO()}")
