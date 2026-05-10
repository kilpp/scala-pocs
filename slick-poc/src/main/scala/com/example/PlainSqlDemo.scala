package com.example

import slick.jdbc.{GetResult, PositionedResult}
import slick.jdbc.H2Profile.api.*

import scala.concurrent.{ExecutionContext, Future}

/** Drops down to `sql"..."` for hand-written queries. Interpolated values are
  * bound as JDBC parameters, not concatenated. A `GetResult[T]` adapter maps
  * each row to a typed result. */
object PlainSqlDemo:

  final case class CountryStat(country: String, totalBooks: Int)

  given GetResult[CountryStat] =
    (r: PositionedResult) => CountryStat(r.nextString(), r.nextInt())

  def run(db: Database)(using ExecutionContext): Future[Unit] =
    val perCountry =
      sql"""
        SELECT a.country, COUNT(b.id)
          FROM authors a
          LEFT JOIN books b ON b.author_id = a.id
         GROUP BY a.country
         ORDER BY a.country
      """.as[CountryStat]

    val target = "USA"
    val byCountry =
      sql"""SELECT a.name FROM authors a WHERE a.country = $target""".as[String]

    for
      stats <- db.run(perCountry)
      names <- db.run(byCountry)
    yield
      println("  per-country totals:")
      stats.foreach(s => println(f"    ${s.country}%-12s ${s.totalBooks}%d"))
      println(s"  authors in $target: ${names.mkString(", ")}")
