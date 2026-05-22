package com.example

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client4.*
import sttp.client4.circe.*
import sttp.model.StatusCode
import sttp.tapir.server.stub4.TapirStubInterpreter
import io.circe.generic.auto.*

class EndpointsSpec extends AnyFunSuite with Matchers:
  private def newBackend: Backend[IO] =
    val store = TaskStore.empty.unsafeRunSync()
    TapirStubInterpreter[IO, Any](BackendStub(new sttp.client4.impl.cats.CatsMonadAsyncError[IO]))
      .whenServerEndpointsRunLogic(ServerEndpoints.all(store))
      .backend()

  test("GET /health returns OK") {
    val resp = basicRequest
      .get(uri"http://test.example/health")
      .response(asStringAlways)
      .send(newBackend)
      .unsafeRunSync()

    resp.code shouldBe StatusCode.Ok
    resp.body shouldBe "OK"
  }

  test("GET /hello/Ada?times=2 repeats greeting") {
    val resp = basicRequest
      .get(uri"http://test.example/hello/Ada?times=2")
      .response(asJson[Greeting])
      .send(newBackend)
      .unsafeRunSync()

    resp.code shouldBe StatusCode.Ok
    resp.body.toOption.map(_.message) shouldBe Some("Hello, Ada! Hello, Ada!")
  }

  test("Tasks lifecycle: create, fetch, list, delete") {
    val backend = newBackend

    val created = basicRequest
      .post(uri"http://test.example/tasks")
      .body(CreateTask("write tapir poc"))
      .response(asJson[Task])
      .send(backend)
      .unsafeRunSync()

    created.code shouldBe StatusCode.Created
    val task = created.body.toOption.value
    task.title shouldBe "write tapir poc"
    task.done shouldBe false

    val fetched = basicRequest
      .get(uri"http://test.example/tasks/${task.id}")
      .response(asJson[Task])
      .send(backend)
      .unsafeRunSync()
    fetched.code shouldBe StatusCode.Ok
    fetched.body.toOption shouldBe Some(task)

    val listed = basicRequest
      .get(uri"http://test.example/tasks")
      .response(asJson[List[Task]])
      .send(backend)
      .unsafeRunSync()
    listed.body.toOption shouldBe Some(List(task))

    val deleted = basicRequest
      .delete(uri"http://test.example/tasks/${task.id}")
      .response(asStringAlways)
      .send(backend)
      .unsafeRunSync()
    deleted.code shouldBe StatusCode.NoContent

    val missing = basicRequest
      .get(uri"http://test.example/tasks/${task.id}")
      .response(asStringAlways)
      .send(backend)
      .unsafeRunSync()
    missing.code shouldBe StatusCode.NotFound
  }

  test("POST /tasks with blank title returns 400 error body") {
    val resp = basicRequest
      .post(uri"http://test.example/tasks")
      .body(CreateTask("   "))
      .response(asJson[ErrorInfo])
      .send(newBackend)
      .unsafeRunSync()

    resp.body.toOption.map(_.message) shouldBe Some("title must not be blank")
  }

  // tiny helper so we don't need EitherValues import
  extension [A, B](e: Either[A, B]) private def value: B = e.toOption.get
