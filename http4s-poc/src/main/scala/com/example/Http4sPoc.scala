package com.example

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{host, port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

object Http4sPoc extends IOApp.Simple:
  def run: IO[Unit] =
    TaskStore.empty.flatMap { store =>
      val app = Logger.httpApp(logHeaders = true, logBody = false)(
        Routes.all(store).orNotFound
      )
      EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
        .use { server =>
          IO.println(s"Listening on ${server.address}") *> IO.never
        }
    }
