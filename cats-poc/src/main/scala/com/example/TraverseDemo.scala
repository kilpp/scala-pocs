package com.example

import cats.syntax.all.*

object TraverseDemo:
  private def parseAge(s: String): Either[String, Int] =
    s.toIntOption.toRight(s"not a number: $s")

  def run(): Unit =
    println("--- TraverseDemo ---")

    // traverse: turn List[A] => List[F[B]] => F[List[B]] in one pass
    val allValid = List("10", "20", "30").traverse(parseAge)
    val oneBad   = List("10", "twenty", "30").traverse(parseAge)
    println(s"traverse all valid: $allValid")
    println(s"traverse one bad:   $oneBad")

    // sequence: List[Option[A]] => Option[List[A]]
    val seqOk:  Option[List[Int]] = List(Option(1), Option(2), Option(3)).sequence
    val seqBad: Option[List[Int]] = List(Option(1), None, Option(3)).sequence
    println(s"sequence options ok:  $seqOk")
    println(s"sequence options bad: $seqBad")

    // foldMap with Monoid
    val totalChars = List("cats", "are", "cool").foldMap(_.length)
    println(s"foldMap length sum:   $totalChars")
    println()
