//> using scala 3.7.3

enum Free[F[_], A]:
  case Pure(a: A)                          extends Free[F, A]
  case Suspend(fa: F[A])                   extends Free[F, A]
  case FlatMap[F[_], B, A](fb: Free[F, B], f: B => Free[F, A]) extends Free[F, A]

  def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap(this, f)
  def map[B](f: A => B): Free[F, B] = flatMap(a => Pure(f(a)))

object Free:
  def liftF[F[_], A](fa: F[A]): Free[F, A] = Suspend(fa)
  def pure[F[_], A](a: A): Free[F, A] = Pure(a)

trait ~>[F[_], G[_]]:
  def apply[A](fa: F[A]): G[A]

def foldMap[F[_], G[_], A](program: Free[F, A], nat: F ~> G)(using m: Monad[G]): G[A] =
  program match
    case Free.Pure(a)         => m.pure(a)
    case Free.Suspend(fa)     => nat(fa)
    case Free.FlatMap(fb, f)  => m.flatMap(foldMap(fb, nat))(b => foldMap(f(b), nat))

trait Monad[F[_]]:
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

given Monad[[X] =>> X] with
  def pure[A](a: A): A = a
  def flatMap[A, B](fa: A)(f: A => B): B = f(fa)

enum KV[A]:
  case Get(key: String) extends KV[Option[String]]
  case Put(key: String, value: String) extends KV[Unit]

import KV.*
def get(k: String): Free[KV, Option[String]] = Free.liftF(Get(k))
def put(k: String, v: String): Free[KV, Unit] = Free.liftF(Put(k, v))

@main def freeMonad(): Unit =
  val program: Free[KV, Option[String]] =
    for
      _    <- put("a", "1")
      _    <- put("b", "2")
      a    <- get("a")
      _    <- put("c", a.getOrElse("?"))
      last <- get("c")
    yield last

  val store = scala.collection.mutable.Map.empty[String, String]
  type Id[A] = A
  val interpreter: KV ~> Id = new (KV ~> Id):
    def apply[A](op: KV[A]): Id[A] = op match
      case Get(k)    => store.get(k)
      case Put(k, v) => store(k) = v
  val result = foldMap[KV, Id, Option[String]](program, interpreter)
  println(s"store = $store")
  println(s"result = $result")
