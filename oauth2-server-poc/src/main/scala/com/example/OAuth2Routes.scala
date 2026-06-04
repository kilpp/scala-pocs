package com.example

import cats.effect.IO
import io.circe.Json
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.{BasicCredentials, HttpRoutes, Request, Response, Status, UrlForm}
import pdi.jwt.JwtClaim

/** The HTTP surface of the authorization server: the token, introspection, and
  * JWKS endpoints, expressed as plain http4s routes over [[Clients]] and
  * [[TokenService]].
  */
final class OAuth2Routes(clients: Clients, tokenService: TokenService):

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    // RFC 6749 §4.4 — client_credentials grant.
    case req @ POST -> Root / "oauth2" / "token" =>
      withForm(req) { form =>
        form.getFirst("grant_type") match
          case Some("client_credentials") => clientCredentialsGrant(req, form)
          case Some(other) =>
            oauthError(Status.BadRequest, "unsupported_grant_type", s"grant_type '$other' is not supported")
          case None =>
            oauthError(Status.BadRequest, "invalid_request", "missing grant_type")
      }

    // RFC 7662 — token introspection.
    case req @ POST -> Root / "oauth2" / "introspect" =>
      withForm(req) { form =>
        val response = form.getFirst("token").flatMap(tokenService.verify) match
          case Some(claim) => introspection(claim)
          case None        => Json.obj("active" -> Json.fromBoolean(false))
        json(Status.Ok, response)
      }

    // RFC 7517 — published verification keys.
    case GET -> Root / ".well-known" / "jwks.json" =>
      json(Status.Ok, tokenService.jwks)
  }

  /** Decode the form body, answering a 400 `invalid_request` instead of a 500 if
    * it is malformed (e.g. not `application/x-www-form-urlencoded`).
    */
  private def withForm(req: Request[IO])(f: UrlForm => IO[Response[IO]]): IO[Response[IO]] =
    req.as[UrlForm].attempt.flatMap {
      case Right(form) => f(form)
      case Left(_)     => oauthError(Status.BadRequest, "invalid_request", "malformed request body")
    }

  private def clientCredentialsGrant(req: Request[IO], form: UrlForm): IO[Response[IO]] =
    clientCredentials(req, form).flatMap((id, secret) => clients.authenticate(id, secret)) match
      case None =>
        oauthError(Status.Unauthorized, "invalid_client", "client authentication failed")
      case Some(client) =>
        val requested = form.getFirst("scope").map(parseScopes).getOrElse(Set.empty)
        clients.resolveScopes(client, requested) match
          case Left(message) =>
            oauthError(Status.BadRequest, "invalid_scope", message)
          case Right(scopes) =>
            val (token, ttl) = tokenService.issue(client.id, scopes)
            json(
              Status.Ok,
              Json.obj(
                "access_token" -> Json.fromString(token),
                "token_type"   -> Json.fromString("Bearer"),
                "expires_in"   -> Json.fromLong(ttl),
                "scope"        -> Json.fromString(scopes.toSeq.sorted.mkString(" "))
              )
            )

  /** Client credentials, preferring HTTP Basic auth (`client_secret_basic`) and
    * falling back to form fields (`client_secret_post`).
    */
  private def clientCredentials(req: Request[IO], form: UrlForm): Option[(String, String)] =
    req.headers
      .get[Authorization]
      .collect { case Authorization(BasicCredentials(user, pass)) => (user, pass) }
      .orElse(
        for
          id     <- form.getFirst("client_id")
          secret <- form.getFirst("client_secret")
        yield (id, secret)
      )

  private def parseScopes(raw: String): Set[String] =
    raw.split("\\s+").filter(_.nonEmpty).toSet

  /** Project a verified JWT into an RFC 7662 introspection response: the standard
    * registered claims plus the custom claims (`scope`, `client_id`).
    */
  private def introspection(claim: JwtClaim): Json =
    val base = Json.obj(
      "active"     -> Json.fromBoolean(true),
      "token_type" -> Json.fromString("Bearer"),
      "sub"        -> claim.subject.fold(Json.Null)(Json.fromString),
      "iss"        -> claim.issuer.fold(Json.Null)(Json.fromString),
      "aud"        -> claim.audience.fold(Json.Null)(a => Json.fromValues(a.toSeq.map(Json.fromString))),
      "exp"        -> claim.expiration.fold(Json.Null)(Json.fromLong),
      "iat"        -> claim.issuedAt.fold(Json.Null)(Json.fromLong),
      "jti"        -> claim.jwtId.fold(Json.Null)(Json.fromString)
    )
    io.circe.parser.parse(claim.content).getOrElse(Json.obj()).deepMerge(base)

  private def json(status: Status, body: Json): IO[Response[IO]] =
    IO.pure(Response[IO](status).withEntity(body))

  /** OAuth2 error response (RFC 6749 §5.2). */
  private def oauthError(status: Status, error: String, description: String): IO[Response[IO]] =
    json(status, Json.obj("error" -> Json.fromString(error), "error_description" -> Json.fromString(description)))
