//> using scala 3.7.3

import scala.compiletime.{constValue, error, codeOf}
import scala.compiletime.ops.int.*

inline def square(inline x: Int): Int = x * x

inline def power(inline base: Int, inline exp: Int): Int =
  inline if exp <= 0 then 1
  else base * power(base, exp - 1)

inline def assertPositive(inline n: Int): Int =
  inline if n <= 0 then error("expected positive int, got " + codeOf(n))
  else n

type Plus[A <: Int, B <: Int] = A + B
type Mult[A <: Int, B <: Int] = A * B

inline def whatIs(inline expr: Any): String =
  "expr: " + codeOf(expr)

inline def repeat(inline n: Int)(inline body: => Unit): Unit =
  inline if n <= 0 then ()
  else
    body
    repeat(n - 1)(body)

@main def macros(): Unit =
  println(square(7))
  println(power(2, 10))

  val safe = assertPositive(42)
  println(safe)

  val sum: 5 = constValue[Plus[2, 3]]
  val prod: 12 = constValue[Mult[3, 4]]
  println(s"$sum and $prod")

  println(whatIs(1 + 2 * 3))

  var count = 0
  repeat(4) { count += 1 }
  println(s"counted $count")
