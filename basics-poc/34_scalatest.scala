//> using scala 3.7.3
//> using dep org.scalatest::scalatest::3.2.20
//> using dep org.scalatest::scalatest-shouldmatchers::3.2.20

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite

class CalculatorSpec extends AnyFlatSpec, Matchers:
  "addition" should "sum two numbers" in:
    (1 + 2) shouldBe 3

  it should "be commutative" in:
    (3 + 4) shouldBe (4 + 3)

  "a list" should "support filter and map" in:
    val xs = List(1, 2, 3, 4)
    xs.filter(_ % 2 == 0) shouldBe List(2, 4)
    xs.map(_ * 10).sum shouldBe 100

  "an exception" should "be thrown on bad input" in:
    an [NumberFormatException] should be thrownBy "abc".toInt

class StringSuite extends AnyFunSuite:
  test("reverse twice is identity"):
    val s = "scala"
    assert(s.reverse.reverse == s)

  test("upper changes case"):
    assert("hello".toUpperCase == "HELLO")

@main def scalatestRun(): Unit =
  org.scalatest.run(new CalculatorSpec)
  org.scalatest.run(new StringSuite)
