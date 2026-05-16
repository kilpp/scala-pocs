package com.example

import com.example.config.AppConfig
import com.example.db.Database
import com.example.http.OrderRoutes
import com.example.messaging.MessageBus
import com.example.schema.{Order, OrderStatus}
import com.example.stream.OrderProcessor
import zio.*
import zio.config.magnolia.*
import zio.config.typesafe.*
import zio.http.*
import zio.logging.backend.SLF4J

object ZioPoc extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: Task[Unit] =
    // Load config eagerly so we can build all layers (including Server) together
    for
      cfg <- TypesafeConfigProvider.fromResourcePath()
               .load(deriveConfig[AppConfig].nested("app"))
               .mapError(e => new Exception(e.toString))
      _   <- program.provide(
               ZLayer.succeed(cfg),
               Database.live,
               MessageBus.live,
               Server.defaultWithPort(cfg.http.port)
             )
    yield ()

  private val program: RIO[AppConfig & Database & MessageBus & Server, Unit] =
    for
      // 1. Init DB schema
      _              <- Database.init
      _              <- ZIO.logInfo("Database initialised")

      // 2. Seed sample orders
      _              <- seedOrders

      // 3. Stream processor fiber — subscribes to bus and updates DB
      processorFiber <- OrderProcessor.run.fork
      _              <- ZIO.logInfo("Order processor fiber started")

      // 4. Publish an order to the message bus
      _              <- publishSampleOrder

      // 5. Give the processor a moment
      _              <- ZIO.sleep(300.millis)

      // 6. HTTP server — auto-stops after 30 s so `sbt run` doesn't hang in a POC
      _              <- ZIO.logInfo("HTTP server starting on port 8080 (auto-stops in 30 s)")
      _              <- Server.serve(OrderRoutes.routes)
                          .race(ZIO.sleep(30.seconds) *> ZIO.logInfo("30-s timer expired"))

      // 7. Clean up
      _              <- processorFiber.interrupt
      _              <- ZIO.logInfo("Done")
    yield ()

  private val seedOrders: RIO[Database, Unit] =
    ZIO.foreachDiscard(List(
      Order(1, "Laptop",   2, 999.99, OrderStatus.Pending),
      Order(2, "Mouse",    5,  29.99, OrderStatus.Pending),
      Order(3, "Keyboard", 3,  79.99, OrderStatus.Pending),
    ))(Database.insert)

  private val publishSampleOrder: RIO[MessageBus & Database, Unit] =
    val order = Order(4, "Monitor", 1, 399.99, OrderStatus.Pending)
    Database.insert(order)
      *> MessageBus.publish(order)
      *> ZIO.logInfo(s"Published order ${order.id} to bus")
