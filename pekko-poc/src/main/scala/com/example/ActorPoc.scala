package com.example

import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler, SupervisorStrategy}
import org.apache.pekko.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.*

/** Demonstrates typed actors, the ask pattern, and supervision. */
object ActorPoc:

  object Greeter:
    sealed trait Command
    final case class Greet(name: String, replyTo: ActorRef[Greeted]) extends Command
    final case class Greeted(message: String, from: String)

    def apply(): Behavior[Command] = Behaviors.setup { ctx =>
      ctx.log.info("greeter ready")
      Behaviors.receiveMessage {
        case Greet(name, replyTo) =>
          replyTo ! Greeted(s"Hello, $name!", ctx.self.path.name)
          Behaviors.same
      }
    }

  object Counter:
    sealed trait Command
    case object Increment                              extends Command
    final case class GetValue(replyTo: ActorRef[Int])  extends Command
    case object Boom                                   extends Command

    def apply(): Behavior[Command] =
      Behaviors
        .supervise(stateful(0))
        .onFailure[RuntimeException](SupervisorStrategy.restart)

    private def stateful(state: Int): Behavior[Command] =
      Behaviors.receive { (ctx, msg) =>
        msg match
          case Increment =>
            ctx.log.info(s"counter $state -> ${state + 1}")
            stateful(state + 1)
          case GetValue(replyTo) =>
            replyTo ! state
            Behaviors.same
          case Boom =>
            throw RuntimeException("intentional crash")
      }

  def run(): Unit =
    val system = ActorSystem[Nothing](
      Behaviors.setup[Nothing] { ctx =>
        given Scheduler = ctx.system.scheduler
        given Timeout   = 3.seconds
        import ctx.executionContext

        val greeter = ctx.spawn(Greeter(), "greeter")
        val counter = ctx.spawn(Counter(), "counter")

        val log = ctx.system.log

        greeter
          .ask[Greeter.Greeted](r => Greeter.Greet("Pekko", r))
          .foreach(reply => log.info(s"greeter reply: ${reply.message} (from ${reply.from})"))

        counter ! Counter.Increment
        counter ! Counter.Increment
        counter ! Counter.Boom // restart resets state
        counter ! Counter.Increment

        counter
          .ask[Int](Counter.GetValue.apply)
          .foreach(v => log.info(s"counter value after restart = $v"))

        Behaviors.empty
      },
      "actor-poc"
    )

    Thread.sleep(2000)
    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
