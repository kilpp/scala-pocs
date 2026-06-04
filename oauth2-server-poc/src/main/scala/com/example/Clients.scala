package com.example

/** A registered OAuth2 client (RFC 6749 §2). For the POC, secrets are kept in
  * plaintext in memory; a real server would store only a salted hash.
  *
  * @param id            the `client_id`
  * @param secret        the `client_secret`
  * @param allowedScopes scopes this client may request via `client_credentials`
  */
final case class Client(id: String, secret: String, allowedScopes: Set[String])

/** In-memory client registry. Seeded with two demo clients so the POC has
  * something to authenticate without any external store.
  */
final class Clients(registry: Map[String, Client]):

  /** Look up a client and verify its secret in one step. Returns `None` for
    * both "unknown client" and "wrong secret" so callers cannot distinguish the
    * two (avoids a client-enumeration oracle).
    */
  def authenticate(clientId: String, clientSecret: String): Option[Client] =
    registry.get(clientId).filter(c => constantTimeEquals(c.secret, clientSecret))

  /** Validate requested scopes against what the client is allowed. An empty
    * request defaults to the client's full allowance.
    */
  def resolveScopes(client: Client, requested: Set[String]): Either[String, Set[String]] =
    if requested.isEmpty then Right(client.allowedScopes)
    else
      val invalid = requested -- client.allowedScopes
      if invalid.isEmpty then Right(requested)
      else Left(s"unsupported scope(s): ${invalid.toSeq.sorted.mkString(" ")}")

  /** Length-stable comparison to avoid leaking secret length via timing. */
  private def constantTimeEquals(a: String, b: String): Boolean =
    val ab = a.getBytes("UTF-8")
    val bb = b.getBytes("UTF-8")
    var result = ab.length ^ bb.length
    var i = 0
    while i < ab.length && i < bb.length do
      result |= ab(i) ^ bb(i)
      i += 1
    result == 0

object Clients:
  /** Demo clients. `service-a` can mint read+write tokens, `reporting` is
    * read-only — enough to exercise scope validation in the token endpoint.
    */
  val demo: Clients = Clients(
    Map(
      "service-a" -> Client("service-a", "s3cr3t-a", Set("read", "write")),
      "reporting" -> Client("reporting", "s3cr3t-r", Set("read"))
    )
  )
