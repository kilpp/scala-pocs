package com.example

import cats.data.{EitherT, OptionT}
import cats.effect.IO
import cats.effect.unsafe.implicits.global

object TransformersDemo:
  // Imagine these are async repos returning IO of (possibly missing / failing) data.
  private final case class User(id: Long, name: String, accountId: Long)
  private final case class Account(id: Long, balanceCents: Long)

  private def findUser(id: Long): IO[Option[User]] =
    IO.pure(Map(1L -> User(1L, "Ada", 99L)).get(id))

  private def loadAccount(id: Long): IO[Either[String, Account]] =
    IO.pure(
      Map(99L -> Account(99L, 12_345L))
        .get(id)
        .toRight(s"account $id not found")
    )

  // OptionT[IO, A] lets us chain Option-returning effects in a single for-comprehension
  // without nesting IO[Option[IO[Option[…]]]].
  private def userName(id: Long): IO[Option[String]] =
    OptionT(findUser(id)).map(_.name).value

  // EitherT[IO, E, A] is the same idea for Either.
  private def balanceFor(userId: Long): IO[Either[String, Long]] =
    (for
      user    <- OptionT(findUser(userId)).toRight(s"user $userId not found")
      account <- EitherT(loadAccount(user.accountId))
    yield account.balanceCents).value

  def run(): Unit =
    println("--- TransformersDemo ---")
    println(s"userName(1) =      ${userName(1L).unsafeRunSync()}")
    println(s"userName(2) =      ${userName(2L).unsafeRunSync()}")
    println(s"balanceFor(1) =    ${balanceFor(1L).unsafeRunSync()}")
    println(s"balanceFor(2) =    ${balanceFor(2L).unsafeRunSync()}")
    println()
