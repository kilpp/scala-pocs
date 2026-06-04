package com.example

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.{host, port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

/** Boots the authorization server on http://localhost:8080 and keeps it running.
  *
  * Try it once it is up:
  * {{{
  * # client_credentials grant (client_secret_post)
  * curl -s localhost:8080/oauth2/token \
  *   -d grant_type=client_credentials -d client_id=service-a \
  *   -d client_secret=s3cr3t-a -d scope=read
  *
  * # introspect the access_token from above
  * curl -s localhost:8080/oauth2/introspect -d token=<ACCESS_TOKEN>
  *
  * # published verification keys
  * curl -s localhost:8080/.well-known/jwks.json
  * }}}
  */
object OAuth2ServerPoc extends IOApp.Simple:

  private val issuer = "http://localhost:8080"

  private val server: Resource[IO, Server] =
    val tokenService = TokenService(issuer, tokenTtlSeconds = 3600)
    val routes       = OAuth2Routes(Clients.demo, tokenService).routes
    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build

  val run: IO[Unit] =
    server.use { _ =>
      IO.println(s"OAuth2 authorization server listening on $issuer") *>
        IO.println("  POST /oauth2/token        grant_type=client_credentials") *>
        IO.println("  POST /oauth2/introspect   token=<jwt>") *>
        IO.println("  GET  /.well-known/jwks.json") *>
        IO.never
    }
