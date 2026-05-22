package com.example

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import com.comcast.ip4s.{host, port}
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object TapirPoc extends IOApp.Simple:
  def run: IO[Unit] =
    TaskStore.empty.flatMap { store =>
      val apiRoutes: HttpRoutes[IO] =
        Http4sServerInterpreter[IO]().toRoutes(ServerEndpoints.all(store))

      val docsRoutes: HttpRoutes[IO] =
        Http4sServerInterpreter[IO]().toRoutes(
          SwaggerInterpreter().fromEndpoints[IO](Endpoints.all, "Tapir POC", "1.0")
        )

      val app = Router("/" -> (apiRoutes <+> docsRoutes)).orNotFound

      EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
        .use { server =>
          IO.println(s"Listening on ${server.address}, docs at /docs") *> IO.never
        }
    }
