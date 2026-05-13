package com.example

import cats.syntax.all.*

// Showcases features highlighted in the cats 2.13.0 release notes.
object NewIn213Demo:
  def run(): Unit =
    println("--- NewIn213Demo (cats 2.13.0 highlights) ---")

    // leftMapOrKeep: only transform Left when the partial function applies, else keep as-is.
    val e1: Either[String, Int] = Left("timeout")
    val e2: Either[String, Int] = Left("other")
    val e3: Either[String, Int] = Right(42)
    val classify: PartialFunction[String, String] = { case "timeout" => "TRANSIENT" }
    println(s"leftMapOrKeep timeout: ${e1.leftMapOrKeep(classify)}")  // Left(TRANSIENT)
    println(s"leftMapOrKeep other:   ${e2.leftMapOrKeep(classify)}")  // Left(other) unchanged
    println(s"leftMapOrKeep right:   ${e3.leftMapOrKeep(classify)}")  // Right(42)

    // traverseVoid replaces deprecated traverse_ — runs effects, discards results.
    var seen = List.empty[Int]
    val effect: List[Int] => Option[Unit] =
      _.traverseVoid { i =>
        seen = i :: seen
        Option.when(i > 0)(())  // None on zero/negative ⇒ short-circuits
      }
    println(s"traverseVoid happy:    ${effect(List(1, 2, 3))}, seen=${seen.reverse}")
    seen = Nil
    println(s"traverseVoid stops:    ${effect(List(1, 0, 3))}, seen=${seen.reverse}")
    println()
