package com.example

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint

object ServerEndpoints:
  def all(store: TaskStore): List[ServerEndpoint[Any, IO]] =
    List(
      Endpoints.health.serverLogicSuccess[IO](_ => IO.pure("OK")),
      Endpoints.hello.serverLogicSuccess[IO] { case (name, times) =>
        val n = times.filter(_ > 0).getOrElse(1)
        IO.pure(Greeting(List.fill(n)(s"Hello, $name!").mkString(" ")))
      },
      Endpoints.listTasks.serverLogicSuccess[IO](_ => store.list),
      Endpoints.getTask.serverLogic[IO] { id =>
        store.find(id).map(_.toRight(ErrorInfo(s"task $id not found")))
      },
      Endpoints.createTask.serverLogic[IO] { input =>
        if input.title.isBlank
        then IO.pure(Left(ErrorInfo("title must not be blank")))
        else store.create(input).map(Right(_))
      },
      Endpoints.deleteTask.serverLogic[IO] { id =>
        store.delete(id).map {
          case true  => Right(())
          case false => Left(ErrorInfo(s"task $id not found"))
        }
      }
    )
