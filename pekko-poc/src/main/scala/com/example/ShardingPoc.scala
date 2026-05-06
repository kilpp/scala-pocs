package com.example

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import org.apache.pekko.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates Cluster Sharding with a counter entity. Runs as a single-node cluster. */
object ShardingPoc:

  object Counter:
    sealed trait Command
    final case class Inc(by: Int)                       extends Command
    final case class Get(replyTo: ActorRef[Int])        extends Command

    val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Counter")

    def apply(entityId: String): Behavior[Command] = stateful(entityId, 0)

    private def stateful(id: String, state: Int): Behavior[Command] =
      Behaviors.receive { (ctx, msg) =>
        msg match
          case Inc(by) =>
            ctx.log.info(s"[entity=$id] inc by $by -> ${state + by}")
            stateful(id, state + by)
          case Get(replyTo) =>
            replyTo ! state
            Behaviors.same
      }

  private def cfg(port: Int) = ConfigFactory.parseString(
    s"""
       |pekko.actor.provider = "cluster"
       |pekko.actor.allow-java-serialization = on
       |pekko.actor.warn-about-java-serializer-usage = off
       |pekko.remote.artery.canonical.hostname = "127.0.0.1"
       |pekko.remote.artery.canonical.port = $port
       |pekko.cluster.seed-nodes = ["pekko://sharding-poc@127.0.0.1:$port"]
       |pekko.coordinated-shutdown.exit-jvm = off
       |""".stripMargin
  )

  def run(): Unit =
    given system: ActorSystem[Nothing] =
      ActorSystem[Nothing](Behaviors.empty[Nothing], "sharding-poc", cfg(25530))
    given Timeout = 3.seconds

    Thread.sleep(2000) // let single-node cluster reach Up

    val sharding = ClusterSharding(system)
    sharding.init(Entity(Counter.TypeKey)(ctx => Counter(ctx.entityId)))

    val a = sharding.entityRefFor(Counter.TypeKey, "a")
    val b = sharding.entityRefFor(Counter.TypeKey, "b")

    a ! Counter.Inc(1)
    a ! Counter.Inc(2)
    b ! Counter.Inc(10)

    val va = Await.result(a.ask[Int](Counter.Get.apply), 3.seconds)
    val vb = Await.result(b.ask[Int](Counter.Get.apply), 3.seconds)
    system.log.info(s"sharded counters: a=$va, b=$vb")

    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
