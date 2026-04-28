//> using scala 3.7.3

final case class State[S, A](run: S => (S, A)):
  def map[B](f: A => B): State[S, B] = State: s =>
    val (s2, a) = run(s)
    (s2, f(a))
  def flatMap[B](f: A => State[S, B]): State[S, B] = State: s =>
    val (s2, a) = run(s)
    f(a).run(s2)

object State:
  def pure[S, A](a: A): State[S, A] = State(s => (s, a))
  def get[S]: State[S, S] = State(s => (s, s))
  def set[S](s: S): State[S, Unit] = State(_ => (s, ()))
  def modify[S](f: S => S): State[S, Unit] = State(s => (f(s), ()))

def push(n: Int): State[List[Int], Unit] =
  State.modify(stack => n :: stack)

def pop: State[List[Int], Option[Int]] = State:
  case Nil    => (Nil, None)
  case h :: t => (t, Some(h))

@main def stateMonad(): Unit =
  val program: State[List[Int], (Option[Int], Option[Int])] =
    for
      _ <- push(1)
      _ <- push(2)
      _ <- push(3)
      a <- pop
      b <- pop
    yield (a, b)

  val (finalStack, results) = program.run(Nil)
  println(s"results = $results")
  println(s"stack   = $finalStack")

  val counter: State[Int, List[Int]] =
    for
      a <- State.modify[Int](_ + 1).flatMap(_ => State.get[Int])
      b <- State.modify[Int](_ + 10).flatMap(_ => State.get[Int])
      c <- State.modify[Int](_ * 2).flatMap(_ => State.get[Int])
    yield List(a, b, c)
  println(counter.run(0))
