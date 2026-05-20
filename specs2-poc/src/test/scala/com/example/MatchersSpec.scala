package com.example

import org.specs2.mutable.Specification

/**
 * Tour of the matcher library. Every assertion uses `must <matcher>`,
 * which yields a `MatchResult` (a `Result` subtype). Matchers compose
 * with `and`, `or`, and `not`.
 */
class MatchersSpec extends Specification:

  "Equality matchers" >> {
    "be_== / beEqualTo / must_== are equivalent" >> {
      (1 + 1) must be_==(2)
      (1 + 1) must beEqualTo(2)
      (1 + 1) must_== 2
      success
    }

    "beTypedEqualTo enforces same static type" >> {
      // Compiles because both sides are Int. Mixing Int and Long would fail.
      42 must beTypedEqualTo(42)
    }
  }

  "Option / Either / Try matchers" >> {
    "beSome with value matcher" >> {
      Some(42) must beSome(42)
      Option.empty[Int] must beNone
      Some(10) must beSome(be_>(5))
    }

    "beRight / beLeft" >> {
      val ok: Either[String, Int]  = Right(7)
      val err: Either[String, Int] = Left("nope")
      ok  must beRight(7)
      err must beLeft("nope")
    }
  }

  "Collection matchers" >> {
    val xs = List(1, 2, 3, 4)

    "contain elements (in any order)" >> {
      xs must contain(2, 3)
    }
    "contain.exactly enforces both size and elements" >> {
      xs must contain(exactly(1, 2, 3, 4))
    }
    "haveSize and beEmpty" >> {
      xs must haveSize(4)
      List.empty[Int] must beEmpty
    }
    "Map matchers — havePair / haveKey / haveValue" >> {
      val m = Map("a" -> 1, "b" -> 2)
      m must havePair("a" -> 1)
      m must haveKey("b")
      m must haveValue(2)
    }
  }

  "String matchers" >> {
    "specs2-flavoured Scala 3 syntax" >> {
      "hello world" must startWith("hello")
      "hello world" must endWith("world")
      "hello world" must contain("o w")
      "hello world" must beMatching(".*o w.*")
    }
  }

  "Numeric matchers" >> {
    "be_> / be_>= / beBetween / beCloseTo" >> {
      10 must be_>(5)
      10 must be_>=(10)
      5  must beBetween(1, 10)
      3.14 must beCloseTo(3.0 +/- 0.5)
    }
  }

  "Exception matchers" >> {
    "throwAn[E] catches the exception type" >> {
      Calculator().divide(1, 0) must throwAn[ArithmeticException]
    }
    "throwAn[E].like to inspect the message" >> {
      Calculator().divide(1, 0) must throwAn[ArithmeticException].like {
        case e => e.getMessage must contain("zero")
      }
    }
  }

  "Composing matchers" >> {
    "and / or / not" >> {
      10 must (be_>(0) and be_<(100))
      "foo" must (startWith("f") or startWith("g"))
      List(1, 2, 3) must not(contain(99))
    }
  }
