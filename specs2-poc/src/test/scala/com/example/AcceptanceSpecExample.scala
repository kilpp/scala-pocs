package com.example

import org.specs2.Specification

/**
 * Acceptance / immutable style — specs2's signature DSL.
 *
 * The `is` method returns a single `SpecStructure` built from a
 * multi-line interpolated string where `${...}` splices in examples.
 * Pure: no mutable state, no `>>`, no `in`. Great for living docs.
 */
class AcceptanceSpecExample extends Specification:
  def is = s2"""

  Calculator behaviour (acceptance style)

    add returns the sum of two ints        $addReturnsSum
    safeDivide(10, 0) returns None         $safeDivideHandlesZero
    isEven(4) is true                      $isEvenTrue
    isEven(5) is false                     $isEvenFalse
    divide(1, 0) throws ArithmeticException $divideThrows
  """

  private val calc = Calculator()

  def addReturnsSum         = calc.add(2, 3) must_== 5
  def safeDivideHandlesZero = calc.safeDivide(10, 0) must beNone
  def isEvenTrue            = calc.isEven(4) must beTrue
  def isEvenFalse           = calc.isEven(5) must beFalse
  def divideThrows          = calc.divide(1, 0) must throwAn[ArithmeticException]
