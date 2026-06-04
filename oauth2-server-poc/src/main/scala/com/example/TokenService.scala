package com.example

import io.circe.Json
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim, JwtHeader}

import java.math.BigInteger
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.{KeyPairGenerator, MessageDigest}
import java.time.Instant
import java.util.{Base64, UUID}
import scala.util.Try

/** Issues and verifies RS256-signed JWT access tokens, and exposes the signing
  * key as a JWK so resource servers can verify tokens offline.
  *
  * One RSA key pair is generated per process. The `kid` is derived from the
  * public key so it is stable for a given key and lets verifiers pick the right
  * JWK from the set.
  */
final class TokenService(issuer: String, tokenTtlSeconds: Long):

  private val keyPair =
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    gen.generateKeyPair()

  private val privateKey = keyPair.getPrivate.asInstanceOf[RSAPrivateKey]
  private val publicKey  = keyPair.getPublic.asInstanceOf[RSAPublicKey]

  /** Key ID: first 16 bytes of SHA-256 over the modulus, base64url-encoded. */
  val keyId: String =
    val digest = MessageDigest.getInstance("SHA-256").digest(toUnsignedBytes(publicKey.getModulus))
    base64Url(digest.take(16))

  /** Mint a signed access token for a client/scope pair. Returns the compact JWT
    * and the lifetime so the endpoint can fill in `expires_in`.
    */
  def issue(clientId: String, scopes: Set[String]): (String, Long) =
    val now    = Instant.now()
    val claim  = JwtClaim(
      content    = Json.obj("scope" -> Json.fromString(scopes.toSeq.sorted.mkString(" ")), "client_id" -> Json.fromString(clientId)).noSpaces,
      issuer     = Some(issuer),
      subject    = Some(clientId),
      audience   = Some(Set("oauth2-resource")),
      expiration = Some(now.plusSeconds(tokenTtlSeconds).getEpochSecond),
      issuedAt   = Some(now.getEpochSecond),
      jwtId      = Some(UUID.randomUUID().toString)
    )
    val header = JwtHeader(Some(JwtAlgorithm.RS256), Some("JWT"), None, Some(keyId))
    (JwtCirce.encode(header, claim, privateKey), tokenTtlSeconds)

  /** Verify a token's signature and expiry. `Some(claim)` means the token is
    * currently valid; `None` covers a bad signature, expiry, or malformed input.
    */
  def verify(token: String): Option[JwtClaim] =
    JwtCirce.decode(token, publicKey, Seq(JwtAlgorithm.RS256)).toOption

  /** The signing key as a single-key JWK Set (RFC 7517). The modulus `n` and
    * exponent `e` are the unsigned big-endian bytes, base64url without padding.
    */
  def jwks: Json =
    val jwk = Json.obj(
      "kty" -> Json.fromString("RSA"),
      "use" -> Json.fromString("sig"),
      "alg" -> Json.fromString("RS256"),
      "kid" -> Json.fromString(keyId),
      "n"   -> Json.fromString(base64Url(toUnsignedBytes(publicKey.getModulus))),
      "e"   -> Json.fromString(base64Url(toUnsignedBytes(publicKey.getPublicExponent)))
    )
    Json.obj("keys" -> Json.arr(jwk))

  /** Read the `kid` from a token's header without verifying it — handy for tests
    * asserting the issued token points at the published JWK.
    */
  def keyIdOf(token: String): Option[String] =
    Try(JwtCirce.decodeAll(token, publicKey, Seq(JwtAlgorithm.RS256)).get._1.keyId).toOption.flatten

  /** Drop the sign byte that `BigInteger.toByteArray` prepends when the high bit
    * is set, yielding the minimal unsigned big-endian representation JWK wants.
    */
  private def toUnsignedBytes(value: BigInteger): Array[Byte] =
    val raw = value.toByteArray
    if raw.length > 1 && raw(0) == 0 then raw.tail else raw

  private def base64Url(bytes: Array[Byte]): String =
    Base64.getUrlEncoder.withoutPadding.encodeToString(bytes)
