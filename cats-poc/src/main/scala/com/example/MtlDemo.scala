package com.example

import cats.data.{Kleisli, Reader, State, Writer}

object MtlDemo:

  // --- State ---
  // Pure stateful computation: S => (S, A)
  private def push(n: Int): State[List[Int], Unit] =
    State.modify(stack => n :: stack)

  private def pop: State[List[Int], Option[Int]] =
    State { stack =>
      stack match
        case h :: t => (t, Some(h))
        case Nil    => (Nil, None)
    }

  private val stackProgram: State[List[Int], Option[Int]] =
    for
      _ <- push(1)
      _ <- push(2)
      _ <- push(3)
      x <- pop
    yield x

  // --- Reader ---
  // Computation that reads from an environment.
  final case class Config(host: String, port: Int)
  private val url: Reader[Config, String] =
    for
      h <- Reader((c: Config) => c.host)
      p <- Reader((c: Config) => c.port)
    yield s"https://$h:$p"

  // --- Writer ---
  // Computation that accumulates a Monoid alongside the value (a log here).
  private def step(n: Int, label: String): Writer[Vector[String], Int] =
    Writer(Vector(s"step $label = $n"), n)

  private val writerProgram: Writer[Vector[String], Int] =
    for
      a <- step(2, "a")
      b <- step(20, "b")
      c <- step(400, "c")
    yield a + b + c

  // --- Kleisli ---
  // A => F[B] composed cleanly. Handy for layering effects on functions.
  private val parseAge: Kleisli[Option, String, Int] =
    Kleisli(s => s.toIntOption)
  private val checkAdult: Kleisli[Option, Int, Int] =
    Kleisli(age => Option.when(age >= 18)(age))
  private val parseAndCheck: Kleisli[Option, String, Int] =
    parseAge andThen checkAdult

  def run(): Unit =
    println("--- MtlDemo ---")

    val (finalStack, popped) = stackProgram.run(Nil).value
    println(s"State final stack:   $finalStack, popped=$popped")

    val rendered = url.run(Config("api.example.com", 8443))
    println(s"Reader url:          $rendered")

    val (log, total) = writerProgram.run
    println(s"Writer total:        $total")
    log.foreach(line => println(s"  log: $line"))

    println(s"Kleisli '21' adult:   ${parseAndCheck.run("21")}")
    println(s"Kleisli '12' adult:   ${parseAndCheck.run("12")}")
    println(s"Kleisli 'abc' adult:  ${parseAndCheck.run("abc")}")
    println()
