package com.example

import cats.effect.IO
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.implicits.*

class Http4sPocSpec extends CatsEffectSuite:

  private def app =
    TaskStore.empty.map(store => Routes.all(store).orNotFound)

  test("GET /health returns 200 OK") {
    for
      service <- app
      resp    <- service.run(Request[IO](Method.GET, uri"/health"))
      body    <- resp.as[String]
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(body, "OK")
  }

  test("GET /hello/:name greets once by default") {
    for
      service <- app
      resp    <- service.run(Request[IO](Method.GET, uri"/hello/Ada"))
      body    <- resp.as[Greeting]
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(body, Greeting("Hello, Ada!"))
  }

  test("GET /hello/:name?times=3 repeats the greeting") {
    for
      service <- app
      resp    <- service.run(Request[IO](Method.GET, uri"/hello/Ada?times=3"))
      body    <- resp.as[Greeting]
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(body, Greeting("Hello, Ada! Hello, Ada! Hello, Ada!"))
  }

  test("POST /tasks creates a task and GET /tasks/:id returns it") {
    for
      service <- app
      created <- service.run(
        Request[IO](Method.POST, uri"/tasks").withEntity(CreateTask("write POC"))
      )
      task <- created.as[Task]
      fetched <- service.run(Request[IO](Method.GET, Uri.unsafeFromString(s"/tasks/${task.id}")))
      back    <- fetched.as[Task]
    yield
      assertEquals(created.status, Status.Created)
      assertEquals(task.title, "write POC")
      assertEquals(task.done, false)
      assertEquals(fetched.status, Status.Ok)
      assertEquals(back, task)
  }

  test("GET /tasks returns the full list") {
    for
      service <- app
      _       <- service.run(Request[IO](Method.POST, uri"/tasks").withEntity(CreateTask("a")))
      _       <- service.run(Request[IO](Method.POST, uri"/tasks").withEntity(CreateTask("b")))
      resp    <- service.run(Request[IO](Method.GET, uri"/tasks"))
      list    <- resp.as[List[Task]]
    yield
      assertEquals(resp.status, Status.Ok)
      assertEquals(list.map(_.title), List("a", "b"))
  }

  test("DELETE /tasks/:id returns 204, then 404 when re-fetched") {
    for
      service <- app
      created <- service.run(
        Request[IO](Method.POST, uri"/tasks").withEntity(CreateTask("temp"))
      )
      task    <- created.as[Task]
      del     <- service.run(Request[IO](Method.DELETE, Uri.unsafeFromString(s"/tasks/${task.id}")))
      missing <- service.run(Request[IO](Method.GET, Uri.unsafeFromString(s"/tasks/${task.id}")))
    yield
      assertEquals(del.status, Status.NoContent)
      assertEquals(missing.status, Status.NotFound)
  }

  test("GET /tasks/:id with unknown id returns 404") {
    for
      service <- app
      resp    <- service.run(Request[IO](Method.GET, uri"/tasks/999"))
    yield assertEquals(resp.status, Status.NotFound)
  }

  test("Unknown route returns 404") {
    for
      service <- app
      resp    <- service.run(Request[IO](Method.GET, uri"/nope"))
    yield assertEquals(resp.status, Status.NotFound)
  }

  // Use io.circe.Json just to confirm transitive availability for ad-hoc work.
  test("circe Json round-trips a Task") {
    val t   = Task(42, "x", done = true)
    val js  = io.circe.syntax.EncoderOps(t).asJson
    val dec = js.as[Task]
    assertEquals(dec, Right(t))
    val _: Json = js
    IO.unit
  }
