package com.example

import scalaz.*

object FreeMonadDemo:

  // A tiny key-value DSL described as a sum of operations.
  sealed trait KVOp[A]
  final case class Put(key: String, value: String) extends KVOp[Unit]
  final case class Get(key: String)                extends KVOp[Option[String]]
  final case class Delete(key: String)             extends KVOp[Unit]

  type KV[A] = Free[KVOp, A]

  def put(k: String, v: String): KV[Unit]   = Free.liftF(Put(k, v))
  def get(k: String): KV[Option[String]]    = Free.liftF(Get(k))
  def delete(k: String): KV[Unit]           = Free.liftF(Delete(k))

  private val program: KV[Option[String]] = for
    _ <- put("name", "scalaz")
    _ <- put("year", "2026")
    _ <- delete("year")
    n <- get("name")
    y <- get("year")
  yield n.map(name => s"$name (year=${y.getOrElse("?")})")

  // Pure-State interpreter: KVOp ~> State[Map[String,String], *]
  type Store[A] = State[Map[String, String], A]

  val pureInterpreter: KVOp ~> Store = new (KVOp ~> Store):
    def apply[A](op: KVOp[A]): Store[A] = op match
      case Put(k, v)  => State.modify[Map[String, String]](_ + (k -> v))
      case Get(k)     => State.gets[Map[String, String], Option[String]](_.get(k))
      case Delete(k)  => State.modify[Map[String, String]](_ - k)

  def run(): Unit =
    println("=== Free Monad (mini KV DSL) ===")
    val (store, result) = program.foldMap(pureInterpreter).run(Map.empty)
    println(s"final store  -> $store")
    println(s"final result -> $result")
