package com.example

import scalaz.*
import Scalaz.*

object StateReaderWriterDemo:

  private val pushTwo: State[List[Int], Unit] = for
    _ <- State.modify[List[Int]](1 :: _)
    _ <- State.modify[List[Int]](2 :: _)
  yield ()

  final case class Config(prefix: String, suffix: String)
  private val greet: Reader[Config, String] =
    Reader(c => s"${c.prefix}Hello${c.suffix}")

  private val logSquare: Writer[Vector[String], Int] = for
    a <- 4.set(Vector("loaded 4"))
    b <- (a * a).set(Vector(s"squared $a -> ${a * a}"))
  yield b

  def run(): Unit =
    println("=== State / Reader / Writer ===")

    val (finalStack, _) = pushTwo.run(List.empty[Int])
    println(s"State final stack -> $finalStack")

    println(s"Reader greet -> ${greet.run(Config("[", "]"))}")

    val (log, value) = logSquare.run
    println(s"Writer value -> $value")
    log.foreach(line => println(s"  log: $line"))
