package com.example

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import org.apache.pekko.stream.{ClosedShape, ThrottleMode}

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates linear pipelines, throttling/backpressure, and the GraphDSL. */
object StreamPoc:

  def run(): Unit =
    given system: ActorSystem[Nothing] =
      ActorSystem[Nothing](Behaviors.empty[Nothing], "stream-poc")

    val sumF = Source(1 to 10)
      .filter(_ % 2 == 0)
      .map(_ * 10)
      .runWith(Sink.fold(0)(_ + _))
    val sum = Await.result(sumF, 5.seconds)
    system.log.info(s"sum of (1..10).filter(even).map(*10) = $sum")

    val throttled = Source(1 to 5)
      .throttle(2, 1.second, 1, ThrottleMode.Shaping)
      .runForeach(i => system.log.info(s"throttled tick: $i"))
    Await.result(throttled, 10.seconds)

    val graphDone = RunnableGraph
      .fromGraph(GraphDSL.createGraph(Sink.foreach[String](s => system.log.info(s"graph -> $s"))) {
        implicit b => out =>
          import GraphDSL.Implicits.*
          val source    = Source(1 to 4)
          val broadcast = b.add(Broadcast[Int](2))
          val merge     = b.add(Merge[String](2))
          val toEven    = Flow[Int].filter(_ % 2 == 0).map(i => s"even:$i")
          val toOdd     = Flow[Int].filter(_ % 2 != 0).map(i => s"odd:$i")

          source ~> broadcast
          broadcast.out(0) ~> toEven ~> merge.in(0)
          broadcast.out(1) ~> toOdd  ~> merge.in(1)
          merge ~> out
          ClosedShape
      })
      .run()
    Await.result(graphDone, 5.seconds)

    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
