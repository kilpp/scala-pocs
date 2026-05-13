package com.example

import cats.data.{Chain, Ior, NonEmptyList, Validated}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CatsPocSpec extends AnyFunSuite with Matchers:

  test("Semigroup |+| combines maps by combining overlapping values") {
    val merged = Map("a" -> 1, "b" -> 2) |+| Map("b" -> 3, "c" -> 4)
    merged shouldBe Map("a" -> 1, "b" -> 5, "c" -> 4)
  }

  test("Validated accumulates all errors, not just the first") {
    val result = ValidatedDemo.validateUser("", 200, "no-at")
    result match
      case Validated.Invalid(errs) => errs.length shouldBe 3
      case Validated.Valid(_)      => fail("expected Invalid")
  }

  test("Validated passes through when all inputs are valid") {
    val result = ValidatedDemo.validateUser("Ada", 36, "ada@example.com")
    result shouldBe Validated.valid(ValidatedDemo.User("Ada", 36, "ada@example.com"))
  }

  test("traverse short-circuits on first Left") {
    val result = List("1", "oops", "3").traverse(s => s.toIntOption.toRight(s"bad: $s"))
    result shouldBe Left("bad: oops")
  }

  test("sequence flips List[Option[_]] into Option[List[_]]") {
    List(Option(1), Option(2), Option(3)).sequence shouldBe Some(List(1, 2, 3))
    List(Option(1), None, Option(3)).sequence       shouldBe None
  }

  test("IO composes sequentially and recovers from errors") {
    val io: IO[Int] =
      IO.raiseError[Int](new RuntimeException("nope")).handleError(_ => 42)
    io.unsafeRunSync() shouldBe 42
  }

  test("Chain.take/drop work as expected (added in cats 2.13.0)") {
    val c = Chain(1, 2, 3, 4, 5)
    c.take(2)      shouldBe Chain(1, 2)
    c.takeRight(2) shouldBe Chain(4, 5)
    c.drop(2)      shouldBe Chain(3, 4, 5)
    c.dropRight(2) shouldBe Chain(1, 2, 3)
  }

  test("NonEmptyList.distinctBy keeps first occurrence per key (2.13.0)") {
    NonEmptyList.of(3, 1, 4, 1, 5, 9, 2, 6).distinctBy(_ % 3).toList shouldBe List(3, 1, 5)
  }

  test("Ior.Both carries both error and value, leftMap touches only the error side") {
    val both: Ior[String, Int] = Ior.Both("warn", 42)
    both.leftMap(_.toUpperCase) shouldBe Ior.Both("WARN", 42)
    both.toEither               shouldBe Right(42)  // Both prefers Right
  }

  test("Either.leftMapOrKeep only fires when the partial function matches (2.13.0)") {
    val timeout: Either[String, Int] = Left("timeout")
    val other:   Either[String, Int] = Left("other")
    val ok:      Either[String, Int] = Right(1)
    val classify: PartialFunction[String, String] = { case "timeout" => "TRANSIENT" }
    timeout.leftMapOrKeep(classify) shouldBe Left("TRANSIENT")
    other.leftMapOrKeep(classify)   shouldBe Left("other")
    ok.leftMapOrKeep(classify)      shouldBe Right(1)
  }

  test("State monad threads state through pure computations") {
    val prog =
      cats.data.State.modify[Int](_ + 1) *>
        cats.data.State.modify[Int](_ * 10) *>
        cats.data.State.get[Int]
    prog.run(0).value shouldBe ((10, 10))
  }

  test("OptionT lets us chain F[Option[_]] without nesting") {
    val nested: IO[Option[Either[String, Int]]] = IO.pure(Some(Right(7)))
    val unwrapped =
      cats.data.OptionT(nested).flatMapF(e => IO.pure(e.toOption)).value.unsafeRunSync()
    unwrapped shouldBe Some(7)
  }
