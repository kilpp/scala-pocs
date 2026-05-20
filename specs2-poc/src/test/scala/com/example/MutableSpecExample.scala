package com.example

import org.specs2.mutable.Specification

/**
 * Mutable / unit style — closest to ScalaTest's FlatSpec/FunSpec.
 *
 * Tests are written with `>>` (or `in`) blocks and look imperative,
 * but specs2 still collects fragments behind the scenes. Each `>>`
 * block must return a `Result` (the last expression).
 */
class MutableSpecExample extends Specification:

  "A Calculator (mutable style)" >> {
    "add returns the sum of two ints" >> {
      Calculator().add(2, 3) must beEqualTo(5)
    }

    "safeDivide returns None when dividing by zero" >> {
      Calculator().safeDivide(10, 0) must beNone
    }

    "divide throws on division by zero" >> {
      Calculator().divide(1, 0) must throwAn[ArithmeticException]
    }

    "nested contexts share setup via local val" >> {
      val calc = Calculator()
      "isEven(4) is true"   >> { calc.isEven(4) must beTrue  }
      "isEven(5) is false"  >> { calc.isEven(5) must beFalse }
    }
  }
