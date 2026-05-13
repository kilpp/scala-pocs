package com.example

import cats.{Id, ~>}
import cats.free.Free

object FreeMonadDemo:
  // 1. Define an algebra as an ADT of operations.
  sealed trait KVOp[A]
  case class Put(key: String, value: String) extends KVOp[Unit]
  case class Get(key: String)                extends KVOp[Option[String]]
  case class Delete(key: String)             extends KVOp[Unit]

  // 2. Lift each op into Free for clean DSL.
  type KV[A] = Free[KVOp, A]
  private def put(k: String, v: String): KV[Unit]    = Free.liftF(Put(k, v))
  private def get(k: String): KV[Option[String]]     = Free.liftF(Get(k))
  private def delete(k: String): KV[Unit]            = Free.liftF(Delete(k))

  // 3. Write a program in the DSL — no execution yet.
  private val program: KV[(Option[String], Option[String])] =
    for
      _   <- put("greeting", "hello")
      _   <- put("subject",  "world")
      a   <- get("greeting")
      _   <- delete("greeting")
      b   <- get("greeting")
    yield (a, b)

  // 4. Provide an interpreter (natural transformation KVOp ~> Id, backed by a mutable map).
  private def mapInterpreter(store: scala.collection.mutable.Map[String, String]): KVOp ~> Id =
    new (KVOp ~> Id):
      def apply[A](op: KVOp[A]): Id[A] = op match
        case Put(k, v) => store.update(k, v)
        case Get(k)    => store.get(k)
        case Delete(k) => store.remove(k); ()

  def run(): Unit =
    println("--- FreeMonadDemo ---")
    val backing = scala.collection.mutable.Map.empty[String, String]
    val result  = program.foldMap(mapInterpreter(backing))
    println(s"Free program result: $result")
    println(s"Backing store after: ${backing.toMap}")
    println()
