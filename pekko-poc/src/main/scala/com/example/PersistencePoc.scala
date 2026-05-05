package com.example

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.persistence.testkit.PersistenceTestKitPlugin
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import org.apache.pekko.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates event-sourced persistence using the in-memory test journal. */
object PersistencePoc:

  object CounterEntity:
    sealed trait Command
    case object Inc                                  extends Command
    final case class Get(replyTo: ActorRef[Int])     extends Command

    sealed trait Event
    case object Incremented                          extends Event

    final case class State(value: Int)

    def apply(id: String): Behavior[Command] =
      EventSourcedBehavior[Command, Event, State](
        persistenceId  = PersistenceId.ofUniqueId(id),
        emptyState     = State(0),
        commandHandler = (state, cmd) =>
          cmd match
            case Inc          => Effect.persist(Incremented)
            case Get(replyTo) => Effect.reply(replyTo)(state.value)
        ,
        eventHandler = (state, evt) =>
          evt match
            case Incremented => state.copy(value = state.value + 1)
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 10, keepNSnapshots = 2))

  def run(): Unit =
    val cfg = PersistenceTestKitPlugin.config.withFallback(ConfigFactory.load())
    given system: ActorSystem[Nothing] =
      ActorSystem[Nothing](Behaviors.empty[Nothing], "persistence-poc", cfg)
    given Timeout = 3.seconds

    val counter = system.systemActorOf(CounterEntity.apply("counter-1"), "counter")

    counter ! CounterEntity.Inc
    counter ! CounterEntity.Inc
    counter ! CounterEntity.Inc

    val v = Await.result(counter.ask[Int](CounterEntity.Get.apply), 3.seconds)
    system.log.info(s"persisted counter value = $v (events were replayed from journal)")

    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
