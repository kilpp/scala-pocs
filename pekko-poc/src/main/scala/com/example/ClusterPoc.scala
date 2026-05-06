package com.example

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.cluster.ClusterEvent.{MemberEvent, MemberUp}
import org.apache.pekko.cluster.typed.{Cluster, Subscribe}

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates a 2-node cluster (in the same JVM) with member-event subscription. */
object ClusterPoc:

  private def listener: Behavior[MemberEvent] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { evt =>
        ctx.log.info(s"[${ctx.system.name}@${Cluster(ctx.system).selfMember.address.port.getOrElse(-1)}] $evt")
        Behaviors.same
      }
    }

  private def cfg(name: String, port: Int, seedPort: Int) = ConfigFactory.parseString(
    s"""
       |pekko.actor.provider = "cluster"
       |pekko.actor.allow-java-serialization = on
       |pekko.actor.warn-about-java-serializer-usage = off
       |pekko.remote.artery.canonical.hostname = "127.0.0.1"
       |pekko.remote.artery.canonical.port = $port
       |pekko.cluster.seed-nodes = ["pekko://$name@127.0.0.1:$seedPort"]
       |pekko.coordinated-shutdown.exit-jvm = off
       |""".stripMargin
  )

  private def guardian: Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    val l = ctx.spawn(listener, "member-listener")
    Cluster(ctx.system).subscriptions ! Subscribe(l, classOf[MemberEvent])
    Behaviors.empty
  }

  def run(): Unit =
    val name     = "cluster-poc"
    val seedPort = 25520

    val s1 = ActorSystem[Nothing](guardian, name, cfg(name, seedPort, seedPort))
    val s2 = ActorSystem[Nothing](guardian, name, cfg(name, seedPort + 1, seedPort))

    // Wait long enough for members to reach Up
    val deadline = System.currentTimeMillis() + 15000
    while
      val notReady = !Cluster(s1).state.members.exists(m => m.status == MemberUp.getClass) &&
        Cluster(s1).state.members.size < 2
      System.currentTimeMillis() < deadline && notReady
    do Thread.sleep(500)

    Thread.sleep(2000)
    s1.log.info(s"cluster size from s1 = ${Cluster(s1).state.members.size}")
    s2.log.info(s"cluster size from s2 = ${Cluster(s2).state.members.size}")

    s2.terminate(); Await.result(s2.whenTerminated, 10.seconds)
    s1.terminate(); Await.result(s1.whenTerminated, 10.seconds)
