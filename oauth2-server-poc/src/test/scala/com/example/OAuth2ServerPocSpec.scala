package com.example

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s.*
import org.http4s.circe.*
import org.http4s.implicits.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Drives the authorization server end to end through its `HttpApp`, without
  * binding a socket: build a request, run it, assert on status and JSON body.
  */
class OAuth2ServerPocSpec extends AnyFreeSpec with Matchers:

  private val tokenService = TokenService("http://test-issuer", tokenTtlSeconds = 3600)
  private val app          = OAuth2Routes(Clients.demo, tokenService).routes.orNotFound

  private def run(req: Request[IO]): (Status, Json) =
    val resp = app.run(req).unsafeRunSync()
    (resp.status, resp.as[Json].unsafeRunSync())

  private def tokenRequest(fields: (String, String)*): Request[IO] =
    Request[IO](Method.POST, uri"/oauth2/token").withEntity(UrlForm(fields*))

  private def field(json: Json, name: String): String =
    json.hcursor.get[String](name).toOption.getOrElse(fail(s"missing field '$name' in $json"))

  "POST /oauth2/token" - {
    "issues a Bearer access token for valid client_credentials" in {
      val (status, body) = run(
        tokenRequest(
          "grant_type"    -> "client_credentials",
          "client_id"     -> "service-a",
          "client_secret" -> "s3cr3t-a",
          "scope"         -> "read write"
        )
      )
      status shouldBe Status.Ok
      field(body, "token_type") shouldBe "Bearer"
      field(body, "scope") shouldBe "read write"
      body.hcursor.get[Long]("expires_in").toOption shouldBe Some(3600L)
      field(body, "access_token") should not be empty
    }

    "defaults to the client's full scope when none is requested" in {
      val (status, body) = run(
        tokenRequest(
          "grant_type"    -> "client_credentials",
          "client_id"     -> "reporting",
          "client_secret" -> "s3cr3t-r"
        )
      )
      status shouldBe Status.Ok
      field(body, "scope") shouldBe "read"
    }

    "rejects a wrong secret as invalid_client" in {
      val (status, body) = run(
        tokenRequest(
          "grant_type"    -> "client_credentials",
          "client_id"     -> "service-a",
          "client_secret" -> "wrong"
        )
      )
      status shouldBe Status.Unauthorized
      field(body, "error") shouldBe "invalid_client"
    }

    "rejects a scope the client may not request as invalid_scope" in {
      val (status, body) = run(
        tokenRequest(
          "grant_type"    -> "client_credentials",
          "client_id"     -> "reporting",
          "client_secret" -> "s3cr3t-r",
          "scope"         -> "write"
        )
      )
      status shouldBe Status.BadRequest
      field(body, "error") shouldBe "invalid_scope"
    }

    "answers 400 invalid_request for a malformed body rather than 500" in {
      val req = Request[IO](Method.POST, uri"/oauth2/token")
        .withEntity("not a form")
        .withContentType(headers.`Content-Type`(MediaType.application.json))
      val (status, body) = run(req)
      status shouldBe Status.BadRequest
      field(body, "error") shouldBe "invalid_request"
    }

    "rejects an unsupported grant_type" in {
      val (status, body) = run(
        tokenRequest("grant_type" -> "password", "client_id" -> "service-a", "client_secret" -> "s3cr3t-a")
      )
      status shouldBe Status.BadRequest
      field(body, "error") shouldBe "unsupported_grant_type"
    }
  }

  "POST /oauth2/introspect" - {
    "reports a freshly issued token as active with its claims" in {
      val (_, tokenBody) = run(
        tokenRequest(
          "grant_type"    -> "client_credentials",
          "client_id"     -> "service-a",
          "client_secret" -> "s3cr3t-a",
          "scope"         -> "read"
        )
      )
      val accessToken = field(tokenBody, "access_token")

      val (status, body) = run(
        Request[IO](Method.POST, uri"/oauth2/introspect").withEntity(UrlForm("token" -> accessToken))
      )
      status shouldBe Status.Ok
      body.hcursor.get[Boolean]("active").toOption shouldBe Some(true)
      field(body, "scope") shouldBe "read"
      field(body, "sub") shouldBe "service-a"
      field(body, "client_id") shouldBe "service-a"
    }

    "reports an unverifiable token as inactive" in {
      val (status, body) = run(
        Request[IO](Method.POST, uri"/oauth2/introspect").withEntity(UrlForm("token" -> "not-a-real-token"))
      )
      status shouldBe Status.Ok
      body.hcursor.get[Boolean]("active").toOption shouldBe Some(false)
    }
  }

  "GET /.well-known/jwks.json" - {
    "publishes an RSA JWK whose kid matches the issued token's header" in {
      val (status, body) = run(Request[IO](Method.GET, uri"/.well-known/jwks.json"))
      status shouldBe Status.Ok

      val jwk = body.hcursor.downField("keys").downArray
      jwk.get[String]("kty").toOption shouldBe Some("RSA")
      jwk.get[String]("alg").toOption shouldBe Some("RS256")
      val publishedKid = jwk.get[String]("kid").toOption

      val (_, tokenBody) = run(
        tokenRequest(
          "grant_type"    -> "client_credentials",
          "client_id"     -> "service-a",
          "client_secret" -> "s3cr3t-a"
        )
      )
      tokenService.keyIdOf(field(tokenBody, "access_token")) shouldBe publishedKid
    }
  }
