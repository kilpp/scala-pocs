//> using scala 3.7.3

trait Monoid[A]:
  def empty: A
  def combine(x: A, y: A): A

given Monoid[String] with
  def empty: String = ""
  def combine(x: String, y: String): String = x + y

given [A]: Monoid[List[A]] with
  def empty: List[A] = Nil
  def combine(x: List[A], y: List[A]): List[A] = x ++ y

final case class Writer[L, A](log: L, value: A):
  def map[B](f: A => B): Writer[L, B] = Writer(log, f(value))
  def flatMap[B](f: A => Writer[L, B])(using m: Monoid[L]): Writer[L, B] =
    val Writer(l2, b) = f(value)
    Writer(m.combine(log, l2), b)

object Writer:
  def pure[L, A](a: A)(using m: Monoid[L]): Writer[L, A] = Writer(m.empty, a)
  def tell[L](l: L): Writer[L, Unit] = Writer(l, ())

def addLogged(a: Int, b: Int): Writer[List[String], Int] =
  for
    _ <- Writer.tell(List(s"adding $a + $b"))
    s = a + b
    _ <- Writer.tell(List(s"got $s"))
  yield s

def mulLogged(a: Int, b: Int): Writer[List[String], Int] =
  for
    _ <- Writer.tell(List(s"multiplying $a * $b"))
    p = a * b
    _ <- Writer.tell(List(s"got $p"))
  yield p

@main def writerMonad(): Unit =
  val program: Writer[List[String], Int] =
    for
      x <- addLogged(1, 2)
      y <- mulLogged(x, 10)
      z <- addLogged(y, 5)
    yield z

  println(s"final value = ${program.value}")
  println("log:")
  program.log.foreach(line => println(s"  - $line"))
