package com.example.stream

import com.example.db.Database
import com.example.messaging.MessageBus
import com.example.schema.{Order, OrderStatus}
import zio.*

// ZIO Streams: consume bus → update DB status → emit processed events
object OrderProcessor:

  // Runs forever as a background fiber; demonstrates ZStream pipeline operators
  val run: RIO[MessageBus & Database, Unit] =
    MessageBus.subscribe
      .tap(o => ZIO.logInfo(s"[stream] received order ${o.id} (${o.product})"))
      .mapZIO(process)
      .runDrain

  private def process(order: Order): RIO[Database, Order] =
    val next = if order.qty > 0 then OrderStatus.Processing else OrderStatus.Cancelled
    Database.updateStatus(order.id, next)
      *> ZIO.logInfo(s"[stream] order ${order.id} → $next")
      *> ZIO.succeed(order.copy(status = next))
