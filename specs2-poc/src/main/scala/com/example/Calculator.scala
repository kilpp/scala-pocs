package com.example

final class Calculator:
  def add(a: Int, b: Int): Int = a + b
  def divide(a: Int, b: Int): Int =
    if b == 0 then throw new ArithmeticException("division by zero")
    else a / b
  def isEven(n: Int): Boolean = n % 2 == 0
  def safeDivide(a: Int, b: Int): Option[Int] =
    if b == 0 then None else Some(a / b)
