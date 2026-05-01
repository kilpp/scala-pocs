package com.example

import cats.syntax.all.*

object Main:
  def greet(name: String): String =
    s"Hello, $name! (powered by sbt + cats)"

  def sumPositives(xs: List[Int]): Option[Int] =
    xs.traverse(n => Option.when(n >= 0)(n)).map(_.sum)

  @main def run(): Unit =
    println(greet("sbt"))
    println(sumPositives(List(1, 2, 3)))
