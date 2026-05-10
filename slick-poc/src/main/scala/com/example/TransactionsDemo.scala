package com.example

import com.example.Schema.*
import slick.jdbc.H2Profile.api.*

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** Bulk update + delete inside one transaction, plus a deliberately failing
  * `DBIO` that should leave the database untouched. */
object TransactionsDemo:

  def run(db: Database)(using ExecutionContext): Future[Unit] =
    for
      _ <- updateAndDelete(db)
      _ <- failingTransaction(db)
    yield ()

  private def updateAndDelete(db: Database)(using ExecutionContext): Future[Unit] =
    val bumpOldPrices = books.filter(_.year < 1970).map(_.priceCents).update(2000)
    val deleteCheap   = books.filter(_.priceCents < 1300).delete
    val program       = (bumpOldPrices zip deleteCheap).transactionally
    db.run(program).map: (rowsBumped, rowsDeleted) =>
      println(s"  bumped price on $rowsBumped pre-1970 books")
      println(s"  deleted $rowsDeleted cheap books")

  private def failingTransaction(db: Database)(using ExecutionContext): Future[Unit] =
    val insertDoomed = authors += Author(0L, "Doomed", "Nowhere")
    val explode      = DBIO.failed(RuntimeException("rollback please"))
    val program      = insertDoomed.andThen(explode).transactionally
    db.run(program).transformWith {
      case Failure(_) =>
        db.run(authors.filter(_.name === "Doomed").length.result).map: count =>
          println(s"  rollback verified — Doomed rows after failure: $count")
      case Success(_) =>
        Future.failed(IllegalStateException("transaction should have failed"))
    }
