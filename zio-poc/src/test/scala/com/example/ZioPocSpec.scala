package com.example

import com.example.batch.BatchJob
import com.example.config.{AppConfig, BatchConfig, DbConfig, HttpConfig}
import com.example.db.Database
import com.example.messaging.MessageBus
import com.example.schema.{Order, OrderStatus}
import com.example.stream.OrderProcessor
import zio.*
import zio.json.*
import zio.test.*

object ZioPocSpec extends ZIOSpecDefault:

  // ── Layers ──────────────────────────────────────────────────────────────────

  private val testAppConfig: ULayer[AppConfig] =
    ZLayer.succeed(
      AppConfig(
        name  = "test",
        http  = HttpConfig("localhost", 0),
        db    = DbConfig("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true", "org.h2.Driver", "sa", ""),
        batch = BatchConfig(chunkSize = 2)
      )
    )

  private val dbLayer: ZLayer[Any, Throwable, Database] =
    testAppConfig >>> Database.live

  private val fullLayer: ZLayer[Any, Throwable, AppConfig & Database & MessageBus] =
    testAppConfig ++ dbLayer ++ MessageBus.live

  // ── Spec ────────────────────────────────────────────────────────────────────

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ZIO POC")(
      suite("Database")(
        test("insert and findById") {
          for
            _     <- Database.init
            order  = Order(10, "Widget", 3, 9.99, OrderStatus.Pending)
            _     <- Database.insert(order)
            found <- Database.findById(10)
          yield assertTrue(found.contains(order))
        },

        test("findAll returns all inserted orders") {
          for
            _   <- Database.init
            _   <- Database.insert(Order(20, "A", 1, 1.0, OrderStatus.Pending))
            _   <- Database.insert(Order(21, "B", 2, 2.0, OrderStatus.Pending))
            all <- Database.findAll
          yield assertTrue(all.exists(_.id == 20)) && assertTrue(all.exists(_.id == 21))
        },

        test("updateStatus changes the order status") {
          for
            _     <- Database.init
            _     <- Database.insert(Order(30, "C", 1, 5.0, OrderStatus.Pending))
            _     <- Database.updateStatus(30, OrderStatus.Shipped)
            found <- Database.findById(30)
          yield assertTrue(found.map(_.status).contains(OrderStatus.Shipped))
        }
      ).provide(dbLayer),

      suite("MessageBus")(
        test("published order is received by subscriber") {
          val order = Order(100, "Bus-item", 1, 0.0, OrderStatus.Pending)
          for
            bus   <- ZIO.service[MessageBus]
            fiber <- bus.subscribe.take(1).runCollect.fork
            _     <- bus.publish(order)
            chunk <- fiber.join
          yield assertTrue(chunk.headOption.contains(order))
        }
      ).provide(MessageBus.live),

      suite("OrderProcessor stream")(
        test("processing an order moves it to Processing status") {
          val order = Order(200, "StreamItem", 2, 0.0, OrderStatus.Pending)
          for
            _     <- Database.init
            _     <- Database.insert(order)
            fiber <- OrderProcessor.run.fork
            _     <- MessageBus.publish(order)
            // Live.live so the real clock is used inside a ZIO Test context
            _     <- Live.live(ZIO.sleep(200.millis))
            found <- Database.findById(200)
            _     <- fiber.interrupt
          yield assertTrue(found.map(_.status).contains(OrderStatus.Processing))
        }
      ).provide(fullLayer),

      suite("BatchJob")(
        test("ships all Processing orders") {
          for
            _       <- Database.init
            _       <- Database.insert(Order(300, "Batch1", 1, 1.0, OrderStatus.Processing))
            _       <- Database.insert(Order(301, "Batch2", 1, 2.0, OrderStatus.Processing))
            shipped <- BatchJob.run
            all     <- Database.findAll
            batchOrders = all.filter(o => o.id == 300 || o.id == 301)
          yield assertTrue(shipped == 2) &&
                assertTrue(batchOrders.forall(_.status == OrderStatus.Shipped))
        }
      ).provide(fullLayer),

      suite("ZIO Schema / JSON codecs")(
        test("Order round-trips through JSON") {
          val order = Order(999, "JSON-item", 1, 42.0, OrderStatus.Pending)
          val json  = order.toJson
          val back  = json.fromJson[Order]
          assertTrue(back == Right(order))
        },

        test("Order JSON contains expected fields") {
          val order = Order(1, "thing", 1, 1.0, OrderStatus.Shipped)
          val json  = order.toJson
          assertTrue(json.contains("\"id\"")) &&
          assertTrue(json.contains("\"product\"")) &&
          assertTrue(json.contains("Shipped"))
        }
      )
    )
