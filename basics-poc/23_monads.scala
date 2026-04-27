//> using scala 3.7.3

trait Monad[F[_]]:
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(a => pure(f(a)))

given Monad[Option] with
  def pure[A](a: A): Option[A] = Some(a)
  def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa.flatMap(f)

given Monad[List] with
  def pure[A](a: A): List[A] = List(a)
  def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] = fa.flatMap(f)

case class Box[A](value: A):
  def map[B](f: A => B): Box[B] = Box(f(value))
  def flatMap[B](f: A => Box[B]): Box[B] = f(value)

given Monad[Box] with
  def pure[A](a: A): Box[A] = Box(a)
  def flatMap[A, B](fa: Box[A])(f: A => Box[B]): Box[B] = fa.flatMap(f)

def sumF[F[_]](a: F[Int], b: F[Int])(using m: Monad[F]): F[Int] =
  m.flatMap(a)(x => m.map(b)(y => x + y))

@main def monads(): Unit =
  println(sumF[Option](Some(1), Some(2)))
  println(sumF[Option](Option.empty[Int], Some(2)))
  println(sumF(List(1, 2), List(10, 20)))
  println(sumF(Box(3), Box(4)))

  val combined =
    for
      a <- Box(10)
      b <- Box(20)
      c <- Box(30)
    yield a + b + c
  println(combined)
