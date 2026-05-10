package com.example

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

object SlickPoc:

  def main(args: Array[String]): Unit =
    val db = Db.memory("slick_poc_main")
    try
      section("Schema + seed")(SchemaDemo.run(db))
      section("Lifted-embedding queries")(QueryDemo.run(db))
      section("Transactions (commit + rollback)")(TransactionsDemo.run(db))
      section("Plain SQL with parameter binding")(PlainSqlDemo.run(db))
    finally db.close()

  private def section(title: String)(body: => Future[Unit]): Unit =
    println()
    println("=" * 60)
    println(title)
    println("=" * 60)
    Await.result(body, 30.seconds)
