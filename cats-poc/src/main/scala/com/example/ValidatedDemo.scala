package com.example

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

object ValidatedDemo:
  final case class User(name: String, age: Int, email: String)

  enum FormError:
    case EmptyName
    case AgeOutOfRange(age: Int)
    case BadEmail(value: String)

  type Result[A] = ValidatedNec[FormError, A]

  private def validateName(s: String): Result[String] =
    if s.trim.isEmpty then FormError.EmptyName.invalidNec else s.validNec

  private def validateAge(n: Int): Result[Int] =
    if n < 0 || n > 130 then FormError.AgeOutOfRange(n).invalidNec else n.validNec

  private def validateEmail(s: String): Result[String] =
    if s.contains("@") then s.validNec else FormError.BadEmail(s).invalidNec

  def validateUser(name: String, age: Int, email: String): Result[User] =
    (validateName(name), validateAge(age), validateEmail(email)).mapN(User.apply)

  def run(): Unit =
    println("--- ValidatedDemo ---")
    val ok  = validateUser("Ada", 36, "ada@example.com")
    val bad = validateUser("", 200, "no-at-symbol")
    println(s"valid:   $ok")
    println(s"invalid: $bad")
    bad match
      case Validated.Invalid(errs) =>
        println(s"accumulated ${errs.length} error(s):")
        errs.toList.foreach(e => println(s"  - $e"))
      case Validated.Valid(_) => ()
    println()
