//> using scala 3.7.3

trait Show[A]:
  def show(a: A): String

object Show:
  given Show[Int] with
    def show(a: Int): String = s"Int($a)"

  given Show[String] with
    def show(a: String): String = s"\"$a\""

  given [A](using s: Show[A]): Show[List[A]] with
    def show(xs: List[A]): String =
      xs.map(s.show).mkString("[", ", ", "]")

def render[A](a: A)(using s: Show[A]): String = s.show(a)

trait Monoid[A]:
  def empty: A
  def combine(x: A, y: A): A

given Monoid[Int] with
  def empty: Int = 0
  def combine(x: Int, y: Int): Int = x + y

given Monoid[String] with
  def empty: String = ""
  def combine(x: String, y: String): String = x + y

def combineAll[A](xs: List[A])(using m: Monoid[A]): A =
  xs.foldLeft(m.empty)(m.combine)

@main def givensAndUsing(): Unit =
  import Show.given

  println(render(42))
  println(render("hello"))
  println(render(List(1, 2, 3)))
  println(render(List("a", "b", "c")))

  println(combineAll(List(1, 2, 3, 4)))
  println(combineAll(List("foo", "bar", "baz")))
