# slick-poc

A small proof-of-concept showing the main moving parts of [Slick](https://scala-slick.org/) on Scala 3 against an in-memory H2 database — schema definition, lifted-embedding queries, transactions, and plain SQL.

## Stack

| Component  | Version |
| ---------- | ------- |
| Scala      | 3.8.3   |
| sbt        | 1.12.11 |
| Slick      | 3.6.1   |
| H2         | 2.4.240 |
| Logback    | 1.5.32  |
| ScalaTest  | 3.2.20  |

## Running

```bash
sbt run     # run all demos against a fresh in-memory database
sbt test    # run the ScalaTest suite
```

`sbt run` boots an in-memory H2 instance, seeds it once, then walks each demo in turn and prints the results.

## Layout

```
src/main/scala/com/example/
  Models.scala             # Author / Book case classes
  Schema.scala             # Slick Tables + TableQueries + foreign key
  Db.scala                 # H2 in-memory Database factory
  Queries.scala            # shared lifted queries (see ScalaTest gotcha below)
  SchemaDemo.scala         # DDL + transactional seed (returning generated PKs)
  QueryDemo.scala          # filter / sort / inner + left join / groupBy + sum
  TransactionsDemo.scala   # bulk update + delete + rollback on DBIO.failed
  PlainSqlDemo.scala       # sql"..." with GetResult and parameter binding
  SlickPoc.scala           # main runner that calls each demo
src/main/resources/
  logback.xml              # quiets slick/hikari logs to WARN
src/test/scala/com/example/
  SlickPocSpec.scala       # ScalaTest suite, fresh DB per test
```

## What each demo shows

- **SchemaDemo** — composes `(authors.schema ++ books.schema).create`, then a single transactional `for`-comprehension that inserts authors with `returning authors.map(_.id)` and uses the generated ids to insert child books.
- **QueryDemo** — four flavours of query built up as values:
  - `filter` + `sortBy` + `map` projection
  - inner join via `for`-comprehension
  - `joinLeft` returning `Rep[Option[T]]`
  - `groupBy` + `length` + `sum.getOrElse(0)`
- **TransactionsDemo** — `(update zip delete).transactionally` for an atomic pair, then a deliberately failing `DBIO.failed` to prove the insert before it does **not** survive rollback.
- **PlainSqlDemo** — `sql"..."` with a custom `given GetResult[CountryStat]` for typed multi-column rows, plus a parameterised query that binds a Scala value as a JDBC parameter (no string concatenation).

## Test strategy

`SlickPocSpec` extends `AnyFlatSpec with BeforeAndAfterEach` and creates a **new** in-memory H2 database per test (a counter suffix makes the URL unique) so tests are fully isolated despite sharing the seeded schema. Each test waits on `Future`s with a 10-second `Await`. Tests cover:

- seed counts (3 authors, 7 books)
- filter + sort invariants
- inner join row count and author coverage
- left join surfaces authors with no books
- groupBy totals
- transactional rollback discards earlier writes
- bulk update rewrites only matching rows
- plain SQL scalar round-trip and safe parameter binding

## Gotcha: ScalaTest `Matchers` shadows Slick's `===`

`org.scalatest.matchers.should.Matchers` mixes in `org.scalactic.TripleEquals`, which puts a member-level `===` on the spec class. Per Scala 3 resolution rules, that member shadows the extension method `===` Slick defines on `Rep[T]`. The expression `b.authorId === a.id` inside a test class therefore compiles to a runtime `Any` equality check on two `Rep` instances, always evaluates to `false`, and Slick bakes the literal into `WHERE false` — silently producing zero rows and no compile error.

This POC sidesteps the problem by defining shared lifted queries in `Queries` (a plain object with no ScalaTest in scope) and calling them from the spec. If you need an ad-hoc Slick predicate inside a test, build the query in a top-level `object` instead of inline.
