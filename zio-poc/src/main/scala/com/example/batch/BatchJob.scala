package com.example.batch

import com.example.config.AppConfig
import com.example.db.Database
import com.example.schema.{Order, OrderStatus}
import zio.*
import zio.stream.*

// Batch: stream all orders in chunks, update Shipped for Processing ones
object BatchJob:

  val run: RIO[Database & AppConfig, Int] =
    for
      cfg     <- ZIO.serviceWith[AppConfig](_.batch)
      orders  <- Database.findAll
      shipped <- ZStream.fromIterable(orders)
                   .filter(_.status == OrderStatus.Processing)
                   .grouped(cfg.chunkSize)
                   .mapZIO(chunk => processChunk(chunk.toList))
                   .runFold(0)(_ + _)
      _       <- ZIO.logInfo(s"[batch] shipped $shipped orders")
    yield shipped

  private def processChunk(chunk: List[Order]): RIO[Database, Int] =
    ZIO.foreachDiscard(chunk)(o => Database.updateStatus(o.id, OrderStatus.Shipped))
      *> ZIO.logInfo(s"[batch] chunk of ${chunk.size} shipped")
      *> ZIO.succeed(chunk.size)
