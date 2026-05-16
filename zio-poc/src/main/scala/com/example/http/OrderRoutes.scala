package com.example.http

import com.example.batch.BatchJob
import com.example.config.AppConfig
import com.example.db.Database
import com.example.messaging.MessageBus
import com.example.schema.Order
import zio.*
import zio.http.*
import zio.json.*

object OrderRoutes:

  private def die(e: Throwable): Response = Response.internalServerError(e.getMessage)

  val routes: Routes[Database & MessageBus & AppConfig, Nothing] =
    Routes(
      // GET /orders
      Method.GET / "orders" ->
        handler(
          Database.findAll
            .map(orders => Response.json(orders.toJson))
            .catchAll(e => ZIO.succeed(die(e)))
        ),

      // GET /orders/:id
      Method.GET / "orders" / long("id") ->
        handler: (id: Long, _: Request) =>
          Database.findById(id)
            .map:
              case Some(o) => Response.json(o.toJson)
              case None    => Response.notFound(s"Order $id not found")
            .catchAll(e => ZIO.succeed(die(e))),

      // POST /orders  body: {"id":1,"product":"Laptop","qty":2,"price":999.99,"status":"Pending"}
      Method.POST / "orders" ->
        handler: (req: Request) =>
          (for
            body  <- req.body.asString
            order <- ZIO.fromEither(body.fromJson[Order]).mapError(s => new Exception(s))
            _     <- Database.insert(order)
            _     <- MessageBus.publish(order)
          yield Response.json(order.toJson).status(Status.Created))
            .catchAll(e => ZIO.succeed(Response.badRequest(e.getMessage))),

      // POST /orders/batch/ship
      Method.POST / "orders" / "batch" / "ship" ->
        handler(
          BatchJob.run
            .map(n => Response.json(s"""{"shipped":$n}"""))
            .catchAll(e => ZIO.succeed(die(e)))
        )
    )
