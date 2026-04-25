//> using scala 3.7.3

def applyTwice[A](f: A => A, a: A): A = f(f(a))

def adder(n: Int): Int => Int = x => x + n

def onList[A, B](xs: List[A])(f: A => B): List[B] = xs.map(f)

def compose[A, B, C](f: B => C, g: A => B): A => C = a => f(g(a))

def repeat[A](n: Int)(f: A => A): A => A =
  if n <= 0 then identity else compose(repeat(n - 1)(f), f)

@main def higherOrderFunctions(): Unit =
  println(applyTwice[Int](_ + 1, 10))
  println(applyTwice[String](_ + "!", "wow"))

  val plus10 = adder(10)
  val plus100 = adder(100)
  println(plus10(5))
  println(plus100(5))

  println(onList(List(1, 2, 3))(_ * 10))
  println(onList(List("a", "bb", "ccc"))(_.length))

  val incThenStr = compose[Int, Int, String](_.toString, _ + 1)
  println(incThenStr(41))

  val xs = List(1, 2, 3, 4, 5)
  println(xs.map(_ * 2))
  println(xs.filter(_ % 2 == 0))
  println(xs.foldLeft(0)(_ + _))
  println(xs.reduce(_ * _))
  println(xs.exists(_ > 4))
  println(xs.forall(_ > 0))
  println(xs.takeWhile(_ < 4))
  println(xs.dropWhile(_ < 4))

  val incFive = repeat(5)((x: Int) => x + 1)
  println(incFive(0))
