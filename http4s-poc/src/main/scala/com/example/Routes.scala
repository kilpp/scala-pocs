package com.example

import cats.effect.IO
import cats.syntax.all.*
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.given

object Routes:
  private given QueryParamDecoder[Int] =
    QueryParamDecoder[String].emap(s =>
      s.toIntOption.toRight(ParseFailure(s"Invalid int: $s", s))
    )

  private object TimesParam extends OptionalQueryParamDecoderMatcher[Int]("times")

  def health: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" => Ok("OK")
  }

  def hello: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name :? TimesParam(times) =>
      val n = times.filter(_ > 0).getOrElse(1)
      Ok(Greeting(List.fill(n)(s"Hello, $name!").mkString(" ")))
  }

  def tasks(store: TaskStore): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "tasks" =>
      store.list.flatMap(Ok(_))

    case GET -> Root / "tasks" / LongVar(id) =>
      store.find(id).flatMap {
        case Some(task) => Ok(task)
        case None       => NotFound()
      }

    case req @ POST -> Root / "tasks" =>
      for
        input <- req.as[CreateTask]
        task  <- store.create(input)
        resp  <- Created(task)
      yield resp

    case DELETE -> Root / "tasks" / LongVar(id) =>
      store.delete(id).flatMap {
        case true  => NoContent()
        case false => NotFound()
      }
  }

  def all(store: TaskStore): HttpRoutes[IO] =
    health <+> hello <+> tasks(store)
