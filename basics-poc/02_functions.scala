//> using scala 3.7.3

def add(a: Int, b: Int): Int = a + b

def greet(name: String, greeting: String = "Hello"): String =
  s"$greeting, $name!"

def sum(xs: Int*): Int = xs.sum

def compose[A, B, C](f: B => C, g: A => B): A => C =
  a => f(g(a))

def describe(n: Int): String =
  if n < 0 then "negative"
  else if n == 0 then "zero"
  else "positive"

@main def functions(): Unit =
  val square: Int => Int = x => x * x
  val double = (x: Int) => x * 2

  println(add(2, 3))
  println(greet("Ada"))
  println(greet("Ada", greeting = "Hi"))
  println(sum(1, 2, 3, 4, 5))
  println(square(5))
  println(double(5))

  val squareThenDouble = compose(double, square)
  println(squareThenDouble(3))

  println(List(1, 2, 3).map(square))
  println(List(-1, 0, 1).map(describe))
