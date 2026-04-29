//> using scala 3.7.3

final case class Parser[A](run: String => Option[(A, String)]):
  def map[B](f: A => B): Parser[B] = Parser: in =>
    run(in).map((a, rest) => (f(a), rest))

  def flatMap[B](f: A => Parser[B]): Parser[B] = Parser: in =>
    run(in).flatMap((a, rest) => f(a).run(rest))

  def | (other: => Parser[A]): Parser[A] = Parser: in =>
    run(in).orElse(other.run(in))

  def ~[B](other: => Parser[B]): Parser[(A, B)] =
    for a <- this; b <- other yield (a, b)

  def many: Parser[List[A]] = Parser: in =>
    val buf = scala.collection.mutable.ListBuffer.empty[A]
    var rest = in
    var done = false
    while !done do
      run(rest) match
        case Some((a, r)) => buf += a; rest = r
        case None         => done = true
    Some((buf.toList, rest))

object Parser:
  def pure[A](a: A): Parser[A] = Parser(in => Some((a, in)))

  def char(c: Char): Parser[Char] = Parser:
    case s if s.nonEmpty && s.head == c => Some((c, s.tail))
    case _                              => None

  def digit: Parser[Char] = Parser:
    case s if s.nonEmpty && s.head.isDigit => Some((s.head, s.tail))
    case _                                 => None

  def number: Parser[Int] = digit.many.map(_.mkString.toInt)

  def string(target: String): Parser[String] = Parser: in =>
    if in.startsWith(target) then Some((target, in.drop(target.length))) else None

import Parser.*

def expr: Parser[Int] =
  for
    a <- number
    _ <- char('+')
    b <- number
  yield a + b

@main def parserCombinators(): Unit =
  println(char('a').run("abc"))
  println(char('a').run("xyz"))
  println(number.run("123abc"))
  println(expr.run("12+34"))
  println(string("hello").run("hello world"))

  val word = string("hi") | string("bye")
  println(word.run("hi!"))
  println(word.run("bye!"))
  println(word.run("hello"))

  val csv = (number ~ char(',')).map(_._1).many
  println(csv.run("1,2,3,4,5"))
