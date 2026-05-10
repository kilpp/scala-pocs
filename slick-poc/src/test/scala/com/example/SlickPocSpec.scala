package com.example

import com.example.Schema.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.H2Profile.api.*

import java.util.concurrent.atomic.AtomicInteger
import scala.compiletime.uninitialized
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

class SlickPocSpec extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  private val counter         = AtomicInteger(0)
  private var db: Database    = uninitialized

  override def beforeEach(): Unit =
    super.beforeEach()
    db = Db.memory(s"slick_poc_test_${counter.incrementAndGet()}")
    Await.result(SchemaDemo.setup(db), 10.seconds)

  override def afterEach(): Unit =
    try db.close()
    finally super.afterEach()

  private def await[T](f: => Future[T]): T = Await.result(f, 10.seconds)

  "schema setup" should "seed three authors and seven books" in {
    await(db.run(authors.length.result)) shouldBe 3
    await(db.run(books.length.result))   shouldBe 7
  }

  "filter + sort" should "return books since 1970 ordered by year" in {
    val years = await(db.run(
      books.filter(_.year >= 1970).sortBy(_.year.asc).map(_.year).result
    ))
    years should not be empty
    years shouldBe sorted
    all (years) should be >= 1970
  }

  "inner join" should "match every book to its author" in {
    val q = Queries.booksWithAuthors.map { case (b, a) => (a.name, b.title) }
    val rows = await(db.run(q.result))
    rows.size shouldBe 7
    rows.map(_._1).distinct should contain allOf (
      "Ursula K. Le Guin", "Italo Calvino", "Jorge Luis Borges"
    )
  }

  "left join" should "still surface authors that have no books" in {
    val orphanId = await(db.run(
      (authors returning authors.map(_.id)) += Author(0L, "Orphan", "Nowhere")
    ))
    val q = Queries.authorsLeftJoinBooks.map { case (a, b) =>
      (a.id, b.map(_.title))
    }
    val rows = await(db.run(q.result))
    rows.collect { case (id, None) => id } should contain (orphanId)
  }

  "group by" should "count books per author" in {
    val q = books.groupBy(_.authorId).map((id, g) => (id, g.length))
    val totals = await(db.run(q.result)).toMap
    totals.values.sum shouldBe 7
    totals should have size 3
  }

  "transactional rollback" should "discard intermediate writes on failure" in {
    val program = (
      (authors += Author(0L, "Doomed", "Nowhere"))
        .andThen(DBIO.failed(RuntimeException("boom")))
    ).transactionally
    a [RuntimeException] should be thrownBy await(db.run(program))
    await(db.run(Queries.authorsByName("Doomed").length.result)) shouldBe 0
  }

  "bulk update" should "rewrite price for matching rows only" in {
    val updated = await(db.run(
      books.filter(_.year < 1970).map(_.priceCents).update(2000)
    ))
    updated should be > 0
    val pricesPre1970 = await(db.run(
      books.filter(_.year < 1970).map(_.priceCents).result
    ))
    all (pricesPre1970) shouldBe 2000
  }

  "plain SQL" should "round-trip a typed scalar" in {
    val q = sql"SELECT COUNT(*) FROM books".as[Int].head
    await(db.run(q)) shouldBe 7
  }

  it should "bind parameters safely" in {
    val country = "Italy"
    val names   = await(db.run(
      sql"SELECT name FROM authors WHERE country = $country".as[String]
    ))
    names should contain only "Italo Calvino"
  }
