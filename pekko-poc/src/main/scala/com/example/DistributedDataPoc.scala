package com.example

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import org.apache.pekko.cluster.ddata.{PNCounter, PNCounterKey, SelfUniqueAddress}

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates Distributed Data CRDTs (PNCounter). Runs as a single-node cluster. */
object DistributedDataPoc:

  private val key = PNCounterKey("ddata-counter")

  sealed trait Cmd
  case object Read                                                      extends Cmd
  final case class WrappedUpdate(rsp: Replicator.UpdateResponse[PNCounter]) extends Cmd
  final case class WrappedGet(rsp: Replicator.GetResponse[PNCounter])      extends Cmd

  private def behavior: Behavior[Cmd] = Behaviors.setup { ctx =>
    given SelfUniqueAddress = DistributedData(ctx.system).selfUniqueAddress
    val replicator = DistributedData(ctx.system).replicator

    val updateAdapter = ctx.messageAdapter[Replicator.UpdateResponse[PNCounter]](WrappedUpdate.apply)
    val getAdapter    = ctx.messageAdapter[Replicator.GetResponse[PNCounter]](WrappedGet.apply)

    Behaviors.withTimers { timers =>
      (1 to 3).foreach { _ =>
        replicator ! Replicator.Update(key, PNCounter.empty, Replicator.WriteLocal, updateAdapter)(_ :+ 1L)
      }
      timers.startSingleTimer("read-after", Read, 1.second)

      Behaviors.receiveMessage {
        case Read =>
          replicator ! Replicator.Get(key, Replicator.ReadLocal, getAdapter)
          Behaviors.same
        case WrappedUpdate(_: Replicator.UpdateSuccess[PNCounter]) =>
          ctx.log.info("ddata: update ack")
          Behaviors.same
        case WrappedUpdate(other) =>
          ctx.log.info(s"ddata update: $other")
          Behaviors.same
        case WrappedGet(rsp: Replicator.GetSuccess[PNCounter] @unchecked) =>
          ctx.log.info(s"ddata value = ${rsp.get(key).value.longValue}")
          Behaviors.same
        case WrappedGet(other) =>
          ctx.log.info(s"ddata get: $other")
          Behaviors.same
      }
    }
  }

  private def cfg(port: Int) = ConfigFactory.parseString(
    s"""
       |pekko.actor.provider = "cluster"
       |pekko.remote.artery.canonical.hostname = "127.0.0.1"
       |pekko.remote.artery.canonical.port = $port
       |pekko.cluster.seed-nodes = ["pekko://ddata-poc@127.0.0.1:$port"]
       |pekko.coordinated-shutdown.exit-jvm = off
       |""".stripMargin
  )

  def run(): Unit =
    val system = ActorSystem[Cmd](behavior, "ddata-poc", cfg(25540))

    Thread.sleep(4000)
    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
