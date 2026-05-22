package com.example

import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.*

object Endpoints:
  private val base = endpoint.errorOut(
    oneOf[ErrorInfo](
      oneOfVariant(StatusCode.NotFound, jsonBody[ErrorInfo].description("not found")),
      oneOfDefaultVariant(jsonBody[ErrorInfo].description("bad request"))
    )
  )

  val health: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get
      .in("health")
      .out(stringBody)
      .description("Liveness probe")

  val hello: PublicEndpoint[(String, Option[Int]), ErrorInfo, Greeting, Any] =
    base.get
      .in("hello" / path[String]("name"))
      .in(query[Option[Int]]("times").description("number of repetitions"))
      .out(jsonBody[Greeting])

  val listTasks: PublicEndpoint[Unit, ErrorInfo, List[Task], Any] =
    base.get.in("tasks").out(jsonBody[List[Task]])

  val getTask: PublicEndpoint[Long, ErrorInfo, Task, Any] =
    base.get
      .in("tasks" / path[Long]("id"))
      .out(jsonBody[Task])

  val createTask: PublicEndpoint[CreateTask, ErrorInfo, Task, Any] =
    base.post
      .in("tasks")
      .in(jsonBody[CreateTask])
      .out(statusCode(StatusCode.Created).and(jsonBody[Task]))

  val deleteTask: PublicEndpoint[Long, ErrorInfo, Unit, Any] =
    base.delete
      .in("tasks" / path[Long]("id"))
      .out(statusCode(StatusCode.NoContent))

  val all: List[AnyEndpoint] =
    List(health, hello, listTasks, getTask, createTask, deleteTask)
