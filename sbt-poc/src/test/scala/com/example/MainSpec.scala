package com.example

import org.scalatest.funsuite.AnyFunSuite

class MainSpec extends AnyFunSuite:
  test("greet builds the expected message"):
    assert(Main.greet("world") == "Hello, world! (powered by sbt + cats)")

  test("sumPositives returns the sum when all entries are non-negative"):
    assert(Main.sumPositives(List(1, 2, 3)) == Some(6))

  test("sumPositives returns None when any entry is negative"):
    assert(Main.sumPositives(List(1, -1, 2)).isEmpty)
