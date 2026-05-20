package com.example

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

object Specs2Poc:
  def main(args: Array[String]): Unit =
    val calc = Calculator()
    println(s"add(2, 3)         = ${calc.add(2, 3)}")
    println(s"safeDivide(10, 0) = ${calc.safeDivide(10, 0)}")
    println(s"isEven(7)         = ${calc.isEven(7)}")

    val repo = new UserRepository:
      def findById(id: Long): Future[Option[User]] =
        Future.successful(if id == 1L then Some(User(1L, "Ada", "ada@example.com")) else None)
      def save(u: User): Future[User] =
        Future.successful(u.copy(id = 42L))

    val svc = UserService(repo)
    val greeting = Await.result(svc.greet(1L), 2.seconds)
    val saved    = Await.result(svc.register("Linus", "l@kernel.org"), 2.seconds)

    println(s"greet(1)          = $greeting")
    println(s"register(...)     = $saved")
    println()
    println("Run `sbt test` to execute the specs2 specifications.")
