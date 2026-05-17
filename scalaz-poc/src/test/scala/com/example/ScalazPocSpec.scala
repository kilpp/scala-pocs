package com.example

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scalaz.*
import Scalaz.*
import scalaz.effect.IO

import ValidationDemo.RegisterForm
import LensesDemo.{Address, Person, Street}

final class ScalazPocSpec extends AnyFunSuite with Matchers:

  test("Monoid combines integers via |+|") {
    List(1, 2, 3).foldLeft(Monoid[Int].zero)(_ |+| _) shouldBe 6
  }

  test("Monoid combines maps by combining inner values") {
    (Map("a" -> 1) |+| Map("a" -> 2, "b" -> 5)) shouldBe Map("a" -> 3, "b" -> 5)
  }

  test("Applicative |@| sums three options") {
    (1.some |@| 2.some |@| 3.some)(_ + _ + _) shouldBe Some(6)
  }

  test("Traverse short-circuits on None") {
    List(1, 2, 3).traverse(n => if n > 0 then n.some else none[Int]) shouldBe Some(List(1, 2, 3))
    List(1, -1, 3).traverse(n => if n > 0 then n.some else none[Int]) shouldBe None
  }

  test("ValidationNel accumulates all errors") {
    val bad = ValidationDemo.validate(RegisterForm("", 10, "no"))
    bad.isFailure shouldBe true
    bad.swap.toOption.map(_.size) shouldBe Some(3)
  }

  test("ValidationNel succeeds for a fully valid form") {
    val ok = ValidationDemo.validate(RegisterForm("Ada", 36, "ada@x.com"))
    ok.isSuccess shouldBe true
  }

  test("Disjunction for-comp short-circuits on a left") {
    val ok = for { a <- 1.right[String]; b <- 2.right[String] } yield a + b
    ok shouldBe \/-(3)
    val ko = for { _ <- 1.right[String]; _ <- "fail".left[Int] } yield 0
    ko shouldBe -\/("fail")
  }

  test("State threads accumulator") {
    val pushTwo: State[List[Int], Unit] = for
      _ <- State.modify[List[Int]](1 :: _)
      _ <- State.modify[List[Int]](2 :: _)
    yield ()
    pushTwo.run(Nil)._1 shouldBe List(2, 1)
  }

  test("Writer collects log alongside value") {
    val w: Writer[Vector[String], Int] = for
      a <- 4.set(Vector("a"))
      b <- (a + 1).set(Vector("b"))
    yield b
    w.run shouldBe ((Vector("a", "b"), 5))
  }

  test("Free interpreter folds program into final state") {
    import FreeMonadDemo.*
    val program: KV[Option[String]] = for
      _ <- put("k", "v")
      _ <- put("y", "z")
      _ <- delete("y")
      v <- get("k")
    yield v
    val (store, result) = program.foldMap(pureInterpreter).run(Map.empty)
    store shouldBe Map("k" -> "v")
    result shouldBe Some("v")
  }

  test("Lens composition reads and writes nested fields") {
    val ada = Person("Ada", Address("London", Street("Marylebone", 12)))
    val addressL =
      Lens.lensu[Person, Address]((p, a) => p.copy(address = a), _.address)
    val streetL =
      Lens.lensu[Address, Street]((a, s) => a.copy(street = s), _.street)
    val numberL =
      Lens.lensu[Street, Int]((s, n) => s.copy(number = n), _.number)
    val deep = addressL >=> streetL >=> numberL
    deep.get(ada) shouldBe 12
    deep.set(ada, 99).address.street.number shouldBe 99
  }

  test("scalaz-effect IO is referentially transparent") {
    var count = 0
    val io: IO[Int] = IO { count += 1; count }
    io.unsafePerformIO()
    io.unsafePerformIO()
    count shouldBe 2
  }

  test("NonEmptyList preserves head and is never empty by construction") {
    val nel = NonEmptyList(1, 2, 3)
    nel.head shouldBe 1
    nel.size shouldBe 3
  }
