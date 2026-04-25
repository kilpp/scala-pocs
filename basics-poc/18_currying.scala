//> using scala 3.7.3

def add(a: Int)(b: Int): Int = a + b

def multiply(a: Int)(b: Int)(c: Int): Int = a * b * c

def filterBy[A](pred: A => Boolean)(xs: List[A]): List[A] = xs.filter(pred)

@main def currying(): Unit =
  println(add(2)(3))

  val plus5 = add(5)
  println(plus5(10))
  println(plus5(20))

  val timesTen = multiply(10)
  val timesTenAndTwo = timesTen(2)
  println(timesTenAndTwo(3))

  val sumUncurried: (Int, Int) => Int = (a, b) => a + b
  val sumCurried: Int => Int => Int = sumUncurried.curried
  println(sumCurried(7)(8))

  val sumTupled: ((Int, Int)) => Int = sumUncurried.tupled
  println(sumTupled((4, 6)))

  val keepEvens = filterBy[Int](_ % 2 == 0)
  println(keepEvens(List(1, 2, 3, 4, 5, 6)))

  val keepLong = filterBy[String](_.length > 3)
  println(keepLong(List("hi", "hello", "yo", "world")))
