package com.example.messaging

import com.example.schema.Order
import zio.*
import zio.stream.*

// ZIO Queue as an in-process message bus — producer / consumer pattern
trait MessageBus:
  def publish(order: Order): UIO[Unit]
  def subscribe: ZStream[Any, Nothing, Order]

object MessageBus:
  val live: ZLayer[Any, Nothing, MessageBus] =
    ZLayer.fromZIO:
      Queue.unbounded[Order].map(MessageBusLive(_))

  def publish(o: Order): URIO[MessageBus, Unit]              = ZIO.serviceWithZIO(_.publish(o))
  def subscribe: ZStream[MessageBus, Nothing, Order]         = ZStream.serviceWithStream(_.subscribe)

private final class MessageBusLive(queue: Queue[Order]) extends MessageBus:
  def publish(order: Order): UIO[Unit]          = queue.offer(order).unit
  def subscribe: ZStream[Any, Nothing, Order]   = ZStream.fromQueue(queue)
