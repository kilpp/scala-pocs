//> using scala 3.7.3

import scala.language.implicitConversions

case class Money(amount: BigDecimal, currency: String):
  override def toString: String = s"$currency$amount"

implicit def intToMoney(n: Int): Money = Money(BigDecimal(n), "$")

implicit class StringOps(s: String):
  def shoutImplicit: String = s.toUpperCase + "!!!"
  def repeatImplicit(n: Int): String = s * n

implicit val defaultGreeting: String = "Hello"

def greet(name: String)(implicit greeting: String): String =
  s"$greeting, $name"

def maxImpl[A](x: A, y: A)(implicit ord: Ordering[A]): A =
  if ord.gt(x, y) then x else y

@main def implicits(): Unit =
  val m: Money = 50
  println(m)

  println("hi".shoutImplicit)
  println("ab".repeatImplicit(3))

  println(greet("Ada"))
  println(greet("Bob")(using "Hi"))

  println(maxImpl(3, 7))
  println(maxImpl("apple", "banana"))
