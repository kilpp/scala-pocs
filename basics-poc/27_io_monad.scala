//> using scala 3.7.3

final case class IO[A](run: () => A):
  def map[B](f: A => B): IO[B] = IO(() => f(run()))
  def flatMap[B](f: A => IO[B]): IO[B] = IO(() => f(run()).run())

object IO:
  def pure[A](a: A): IO[A] = IO(() => a)
  def delay[A](a: => A): IO[A] = IO(() => a)
  def putStrLn(s: String): IO[Unit] = IO(() => println(s))
  def readConst(s: String): IO[String] = IO(() => s)

@main def ioMonad(): Unit =
  val program: IO[Unit] =
    for
      _    <- IO.putStrLn("What's your name?")
      name <- IO.readConst("Ada")
      _    <- IO.putStrLn(s"Hello, $name!")
    yield ()
  program.run()

  val computation: IO[Int] =
    for
      a <- IO.pure(2)
      b <- IO.pure(3)
      c <- IO.delay(a * b)
      _ <- IO.putStrLn(s"computed $c")
    yield c
  println(s"result = ${computation.run()}")

  val noopUntilRun = IO.delay { println("side effect!"); 42 }
  println("nothing happened yet")
  println(s"now: ${noopUntilRun.run()}")
