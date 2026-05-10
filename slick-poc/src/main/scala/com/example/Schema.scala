package com.example

import slick.jdbc.H2Profile.api.*

object Schema:

  class AuthorsTable(tag: Tag) extends Table[Author](tag, "AUTHORS"):
    def id      = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name    = column[String]("NAME")
    def country = column[String]("COUNTRY")
    def *       = (id, name, country).mapTo[Author]

  class BooksTable(tag: Tag) extends Table[Book](tag, "BOOKS"):
    def id         = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def title      = column[String]("TITLE")
    def authorId   = column[Long]("AUTHOR_ID")
    def priceCents = column[Int]("PRICE_CENTS")
    def year       = column[Int]("YEAR")
    def author     =
      foreignKey("FK_BOOKS_AUTHOR", authorId, authors)(_.id)
    def *          = (id, title, authorId, priceCents, year).mapTo[Book]

  val authors: TableQuery[AuthorsTable] = TableQuery[AuthorsTable]
  val books:   TableQuery[BooksTable]   = TableQuery[BooksTable]
