package com.example

import com.example.Schema.*
import slick.jdbc.H2Profile.api.*

import scala.concurrent.{ExecutionContext, Future}

/** Demonstrates DDL via Slick's lifted embedding plus a transactional batch
  * insert that streams the generated PKs back to populate child rows. */
object SchemaDemo:

  /** Creates the schema and seeds three authors + their books, all inside a
    * single transaction. Returns the generated author ids. */
  def setup(db: Database)(using ExecutionContext): Future[Seq[Long]] =
    val authorsToInsert = Seq(
      Author(0L, "Ursula K. Le Guin", "USA"),
      Author(0L, "Italo Calvino",     "Italy"),
      Author(0L, "Jorge Luis Borges", "Argentina")
    )

    val program = for
      _   <- (authors.schema ++ books.schema).create
      ids <- (authors returning authors.map(_.id)) ++= authorsToInsert
      _   <- books ++= booksFor(ids.toIndexedSeq)
    yield ids

    db.run(program.transactionally)

  private def booksFor(ids: IndexedSeq[Long]): Seq[Book] =
    val leguin  = ids(0)
    val calvino = ids(1)
    val borges  = ids(2)
    Seq(
      Book(0L, "A Wizard of Earthsea",                leguin,  1500, 1968),
      Book(0L, "The Left Hand of Darkness",           leguin,  1800, 1969),
      Book(0L, "The Dispossessed",                    leguin,  1900, 1974),
      Book(0L, "Invisible Cities",                    calvino, 1400, 1972),
      Book(0L, "If on a winter's night a traveler",   calvino, 1700, 1979),
      Book(0L, "Ficciones",                           borges,  1300, 1944),
      Book(0L, "The Aleph",                           borges,  1200, 1949)
    )

  def run(db: Database)(using ExecutionContext): Future[Unit] =
    for
      _   <- setup(db)
      all <- db.run(authors.sortBy(_.id).result)
    yield
      all.foreach(a => println(f"  ${a.id}%2d  ${a.name}%-22s ${a.country}"))
