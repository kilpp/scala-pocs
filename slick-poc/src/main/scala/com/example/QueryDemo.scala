package com.example

import com.example.Schema.*
import slick.jdbc.H2Profile.api.*

import scala.concurrent.{ExecutionContext, Future}

/** Filter / sort / join / groupBy expressed in the lifted embedding. Each
  * step composes a `Query` and only `db.run(_.result)` materialises rows. */
object QueryDemo:

  def run(db: Database)(using ExecutionContext): Future[Unit] =
    for
      _ <- filterAndSort(db)
      _ <- innerJoin(db)
      _ <- leftJoin(db)
      _ <- groupAndAggregate(db)
    yield ()

  private def filterAndSort(db: Database)(using ExecutionContext): Future[Unit] =
    val q = books
      .filter(_.year >= 1970)
      .sortBy(_.year.asc)
      .map(b => (b.year, b.title))
    db.run(q.result).map: rows =>
      println("  filter + sort (books since 1970):")
      rows.foreach((y, t) => println(s"    $y  $t"))

  private def innerJoin(db: Database)(using ExecutionContext): Future[Unit] =
    val q = books
      .join(authors).on((b, a) => b.authorId === a.id)
      .map { case (b, a) => (a.name, b.title) }
      .sortBy((n, _) => n)
    db.run(q.result).map: rows =>
      println("  inner join (author name :: title):")
      rows.foreach((n, t) => println(s"    $n :: $t"))

  private def leftJoin(db: Database)(using ExecutionContext): Future[Unit] =
    val q = authors
      .joinLeft(books).on((a, b) => a.id === b.authorId)
      .map { case (a, b) => (a.name, b.map(_.title)) }
    db.run(q.result).map: rows =>
      println("  left join (every author, even authorless ones):")
      rows.foreach((n, t) => println(s"    $n :: ${t.getOrElse("(no books)")}"))

  private def groupAndAggregate(db: Database)(using ExecutionContext): Future[Unit] =
    val q = books
      .join(authors).on((b, a) => b.authorId === a.id)
      .groupBy { case (_, a) => a.name }
      .map { case (name, g) =>
        (name, g.length, g.map(_._1.priceCents).sum.getOrElse(0))
      }
    db.run(q.sortBy((n, _, _) => n).result).map: rows =>
      println("  group + aggregate (per author totals):")
      rows.foreach((n, c, t) => println(f"    $n%-22s $c%d books, ${t}%5d c"))
