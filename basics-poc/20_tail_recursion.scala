//> using scala 3.7.3

import scala.annotation.tailrec

@tailrec
def factorial(n: BigInt, acc: BigInt = 1): BigInt =
  if n <= 1 then acc
  else factorial(n - 1, acc * n)

@tailrec
def sumList(xs: List[Int], acc: Int = 0): Int = xs match
  case Nil    => acc
  case h :: t => sumList(t, acc + h)

@tailrec
def reverse[A](xs: List[A], acc: List[A] = Nil): List[A] = xs match
  case Nil    => acc
  case h :: t => reverse(t, h :: acc)

@tailrec
def gcd(a: Int, b: Int): Int =
  if b == 0 then a else gcd(b, a % b)

def fib(n: Int): BigInt =
  @tailrec
  def loop(i: Int, a: BigInt, b: BigInt): BigInt =
    if i == 0 then a else loop(i - 1, b, a + b)
  loop(n, 0, 1)

@main def tailRecursion(): Unit =
  println(factorial(20))
  println(factorial(50))
  println(sumList((1 to 1_000_000).toList))
  println(reverse(List(1, 2, 3, 4, 5)))
  println(gcd(48, 18))
  println(fib(50))
