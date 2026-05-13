package com.example

import cats.Eval
import cats.data.{Chain, Ior, NonEmptyChain, NonEmptyList}

object DataTypesDemo:
  def run(): Unit =
    println("--- DataTypesDemo ---")

    // Chain: O(1) concat List. 2.13.0 added take/takeRight/drop/dropRight.
    val ch = Chain("a", "b", "c", "d", "e")
    println(s"Chain:               $ch")
    println(s"Chain ++ Chain:      ${ch ++ Chain("f", "g")}")
    println(s"Chain.take(3):       ${ch.take(3)}")            // new in 2.13.0
    println(s"Chain.takeRight(2):  ${ch.takeRight(2)}")       // new in 2.13.0
    println(s"Chain.drop(2):       ${ch.drop(2)}")            // new in 2.13.0
    println(s"Chain.dropRight(2):  ${ch.dropRight(2)}")       // new in 2.13.0

    // NonEmptyList: head is guaranteed at compile time
    val nel = NonEmptyList.of(3, 1, 4, 1, 5, 9, 2, 6)
    println(s"NonEmptyList.head:   ${nel.head}")        // total, no Option
    println(s"NonEmptyList.reduce: ${nel.reduceLeft(_ + _)}")
    println(s"NEL.distinctBy len:  ${nel.distinctBy(_ % 3).toList}")  // 2.13.0 distinctBy

    // NonEmptyChain: same idea over Chain
    val nec = NonEmptyChain.of("x", "y", "z")
    println(s"NEC append:          ${nec :+ "w"}")

    // Ior: inclusive Either — Left, Right, or *Both*
    val both: Ior[String, Int] = Ior.Both("partial-fail", 42)
    val warn: Ior[String, Int] = both.leftMap(w => s"warn: $w")
    println(s"Ior.Both:            $both")
    println(s"Ior leftMap:         $warn")
    println(s"Ior.toEither:        ${both.toEither}")  // prefers Right value

    // Eval: control evaluation strategy
    var counter = 0
    Eval.now    { counter += 1; "now"    }                   // eager: runs immediately
    val laterE  = Eval.later  { counter += 1; "later"  }     // lazy + memoized
    val alwaysE = Eval.always { counter += 1; "always" }     // lazy, recomputed
    laterE.value; laterE.value          // second call hits the memo
    alwaysE.value; alwaysE.value        // both calls recompute
    println(s"Eval counter total:  $counter   (now=1, later=1, always=2 ⇒ 4)")
    println()
