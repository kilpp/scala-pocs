//> using scala 3.7.3
//> using dep org.scalacheck::scalacheck:1.19.0

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

object Specs extends Properties("Specs"):
  property("string reverse twice is identity") = forAll: (s: String) =>
    s.reverse.reverse == s

  property("string concat length") = forAll: (a: String, b: String) =>
    (a + b).length == a.length + b.length

  property("list sum nonneg of nonneg") = forAll(Gen.listOf(Gen.choose(0, 100))): xs =>
    xs.sum >= 0

  property("list sort idempotent") = forAll: (xs: List[Int]) =>
    xs.sorted == xs.sorted.sorted

  property("filter then map = map then filter on identity") = forAll: (xs: List[Int]) =>
    xs.filter(_ > 0).map(identity) == xs.map(identity).filter(_ > 0)

// Run with: scala-cli run 35_scalacheck.scala
// Properties extends App-like behaviour, so `Specs` is the auto main class.

