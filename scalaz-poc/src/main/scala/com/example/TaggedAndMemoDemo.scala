package com.example

import scalaz.*

object TaggedAndMemoDemo:

  // Tagged types: zero-cost wrappers that make wrong-units a compile error.
  sealed trait UsdCents
  sealed trait Grams

  type Cents = Int @@ UsdCents
  type Mass  = Int @@ Grams

  private def cents(n: Int): Cents = Tag.of[UsdCents](n)
  private def grams(n: Int): Mass  = Tag.of[Grams](n)

  // Memoization
  private val slowSquare: Int => Int = n =>
    Thread.sleep(50); n * n

  private val memoSquare: Int => Int = Memo.immutableHashMapMemo(slowSquare)

  def run(): Unit =
    println("=== Tagged types & Memo ===")

    val price: Cents = cents(199)
    val weight: Mass = grams(250)
    // val mixed = price |+| weight // would not compile -- different tags
    println(s"price -> $price, weight -> $weight (distinct phantom types)")

    val t0 = System.nanoTime()
    memoSquare(11); memoSquare(11); memoSquare(11)
    val firstRun = (System.nanoTime() - t0) / 1_000_000.0

    val t1 = System.nanoTime()
    memoSquare(11); memoSquare(11); memoSquare(11)
    val cached = (System.nanoTime() - t1) / 1_000_000.0

    println(f"Memo: first-call cycle ${firstRun}%.1fms vs cached ${cached}%.1fms")
