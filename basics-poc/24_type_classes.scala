//> using scala 3.7.3

trait Eq[A]:
  def eqv(x: A, y: A): Boolean

object Eq:
  def apply[A](using e: Eq[A]): Eq[A] = e
  given Eq[Int] with
    def eqv(x: Int, y: Int): Boolean = x == y
  given Eq[String] with
    def eqv(x: String, y: String): Boolean = x == y
  given [A](using e: Eq[A]): Eq[List[A]] with
    def eqv(xs: List[A], ys: List[A]): Boolean =
      xs.size == ys.size && xs.zip(ys).forall((a, b) => e.eqv(a, b))

extension [A](x: A)(using e: Eq[A])
  def ===(y: A): Boolean = e.eqv(x, y)

trait Show[A]:
  def show(a: A): String

object Show:
  given Show[Int] with
    def show(a: Int): String = a.toString
  given Show[String] with
    def show(a: String): String = s"\"$a\""
  given [A](using s: Show[A]): Show[Option[A]] with
    def show(o: Option[A]): String = o match
      case Some(a) => s"Some(${s.show(a)})"
      case None    => "None"

def display[A](a: A)(using s: Show[A]): String = s.show(a)

@main def typeClasses(): Unit =
  println(1 === 1)
  println("a" === "b")
  println(List(1, 2, 3) === List(1, 2, 3))
  println(List(1, 2) === List(1, 2, 3))

  println(display(42))
  println(display("hi"))
  println(display[Option[Int]](Some(99)))
  println(display(Option.empty[String]))
