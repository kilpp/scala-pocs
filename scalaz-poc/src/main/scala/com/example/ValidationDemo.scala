package com.example

import scalaz.*
import Scalaz.*

object ValidationDemo:

  final case class RegisterForm(name: String, age: Int, email: String)
  final case class User(name: String, age: Int, email: String)

  private def checkName(s: String): ValidationNel[String, String] =
    if s.nonEmpty then s.successNel else "name must not be empty".failureNel

  private def checkAge(a: Int): ValidationNel[String, Int] =
    if a >= 18 then a.successNel else s"age $a must be >= 18".failureNel

  private def checkEmail(e: String): ValidationNel[String, String] =
    if e.contains("@") then e.successNel else s"email '$e' is invalid".failureNel

  def validate(f: RegisterForm): ValidationNel[String, User] =
    (checkName(f.name) |@| checkAge(f.age) |@| checkEmail(f.email))(User.apply)

  def run(): Unit =
    println("=== Validation (error accumulation) ===")
    val ok  = validate(RegisterForm("Ada", 36, "ada@example.com"))
    val bad = validate(RegisterForm("", 12, "nope"))
    println(s"valid form  -> $ok")
    println(s"broken form -> $bad")

    val viaDisjunction =
      for
        a <- "10".parseInt.toDisjunction
        b <- "32".parseInt.toDisjunction
      yield a + b
    println(s"parseInt via disjunction -> $viaDisjunction")
