package com.example

import com.example.Schema.*
import slick.jdbc.H2Profile.api.*

/** Pre-built lifted queries shared by demos and tests.
  *
  * They live here on purpose — ScalaTest's `Matchers` mixes
  * `org.scalactic.TripleEquals` into the spec class, which puts a member-level
  * `===` in scope. That member would shadow Slick's extension `===` on
  * `Rep[T]` and silently compile `b.authorId === a.id` into a runtime
  * `Any` equality (always `false`), producing `WHERE false` SQL. Building the
  * queries in this object keeps Slick's `===` resolution clean. */
object Queries:

  val booksWithAuthors
      : Query[(Schema.BooksTable, Schema.AuthorsTable), (Book, Author), Seq] =
    books.join(authors).on((b, a) => b.authorId === a.id)

  val authorsLeftJoinBooks
      : Query[
        (Schema.AuthorsTable, Rep[Option[Schema.BooksTable]]),
        (Author, Option[Book]),
        Seq
      ] =
    authors.joinLeft(books).on((a, b) => a.id === b.authorId)

  def authorsByName(name: String): Query[Schema.AuthorsTable, Author, Seq] =
    authors.filter(_.name === name)
