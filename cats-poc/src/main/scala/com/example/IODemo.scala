package com.example

import cats.effect.{IO, IOApp, ExitCode}
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*

import scala.concurrent.duration.*

object IODemo:
  private def log(msg: String): IO[Unit] =
    IO(println(s"[${Thread.currentThread.getName}] $msg"))

  private def slow(label: String, ms: Int): IO[String] =
    log(s"start $label") *>
      IO.sleep(ms.millis) *>
      log(s"done  $label").as(s"$label=$ms")

  private val program: IO[Unit] =
    for
      _  <- log("sequential composition")
      a  <- slow("A", 100)
      b  <- slow("B", 100)
      _  <- log(s"sequential results: $a, $b")
      _  <- log("parallel composition via parTupled")
      (p, q) <- (slow("P", 200), slow("Q", 200)).parTupled
      _  <- log(s"parallel results: $p, $q")
      // error handling
      recovered <- IO.raiseError[Int](new RuntimeException("boom!"))
                     .handleErrorWith(e => log(s"recovered from: ${e.getMessage}").as(-1))
      _  <- log(s"recovered value: $recovered")
    yield ()

  def run(): Unit =
    println("--- IODemo (cats-effect) ---")
    program.unsafeRunSync()
    println()

  // Optional: also runnable as a standalone IOApp via `sbt "runMain com.example.IODemoApp"`
  object App extends IOApp:
    def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
