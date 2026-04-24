//> using scala 3.7.3

import scala.util.{Try, Success, Failure}

def findUser(id: Int): Option[String] =
  val users = Map(1 -> "Ada", 2 -> "Bob")
  users.get(id)

def divide(a: Int, b: Int): Either[String, Int] =
  if b == 0 then Left("division by zero")
  else Right(a / b)

def parseInt(s: String): Try[Int] = Try(s.toInt)

@main def optionEitherTry(): Unit =
  println(findUser(1))
  println(findUser(99))
  println(findUser(1).getOrElse("unknown"))
  println(findUser(1).map(_.toUpperCase))

  findUser(1) match
    case Some(name) => println(s"found $name")
    case None       => println("not found")

  println(divide(10, 2))
  println(divide(10, 0))
  println(divide(10, 2).map(_ * 100))

  divide(10, 0) match
    case Right(v)  => println(s"got $v")
    case Left(err) => println(s"error: $err")

  println(parseInt("42"))
  println(parseInt("oops"))

  parseInt("42") match
    case Success(n) => println(s"parsed $n")
    case Failure(e) => println(s"failed: ${e.getMessage}")

  val combined: Option[Int] =
    for
      x <- findUser(1).map(_.length)
      y <- findUser(2).map(_.length)
    yield x + y
  println(combined)
