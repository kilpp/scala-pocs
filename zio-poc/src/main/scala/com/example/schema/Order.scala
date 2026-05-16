package com.example.schema

import zio.schema.*
import zio.json.*

// ZIO Schema auto-derives codecs, diffs, migrations, and validation
final case class Order(
  id: Long,
  product: String,
  qty: Int,
  price: Double,
  status: OrderStatus
)

enum OrderStatus:
  case Pending, Processing, Shipped, Cancelled

object OrderStatus:
  given Schema[OrderStatus] = Schema.derived
  given JsonCodec[OrderStatus] = JsonCodec.string.transformOrFail(
    s => OrderStatus.values.find(_.toString == s).toRight(s"Unknown status: $s"),
    _.toString
  )

object Order:
  given schema: Schema[Order]    = Schema.derived
  given codec: JsonCodec[Order]  = zio.schema.codec.JsonCodec.jsonCodec(schema)
