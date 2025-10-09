import zio.*
import zio.http.*
import zio.redis.*
import zio.redis.embedded.EmbeddedRedis
import zio.test.*
import zio.test.TestAspect.{parallel, sequential}

object MainTest extends ZIOSpecDefault {

  // Use a live Redis instance for testing
  // This connects to a real Redis server on localhost:6379
  // Make sure Redis is running before running tests: podman run -d -p 6379:6379 redis

  private val redisLayer =
    EmbeddedRedis.layer ++
      ZLayer.succeed[CodecSupplier](Main.ProtobufCodecSupplier) >>>
      Redis.local

  private val appLayer = redisLayer ++ Bitonic.live

  override def spec: Spec[Scope, Any] = suite("Main routes")(
    test("GET /health-check returns Ok") {
      val req = Request.get(URL.decode("/health-check").toOption.get)
      for {
        resp <- Main.routes.runZIO(req)
        body <- resp.body.asString
      } yield assertTrue(resp.status == Status.Ok, body == "Ok")
    },
    test("GET /bitonic returns array and caches on second call") {
      val req = Request.get(URL.decode("/bitonic?n=5&l=1&r=10").toOption.get)
      for {
        resp1 <- Main.routes.runZIO(req)
        body1 <- resp1.body.asString
        cache1 = resp1.headers.get("X-Cache").getOrElse("")
        resp2 <- Main.routes.runZIO(req)
        body2 <- resp2.body.asString
        cache2 = resp2.headers.get("X-Cache").getOrElse("")
      } yield assertTrue(
        body1.startsWith("Array("),
        body1 == body2,
        cache1 == "MISS",
        cache2 == "HIT"
      )
    },
    test("GET /bitonic impossible case returns Array(-1)") {
      val req = Request.get(URL.decode("/bitonic?n=999&l=1&r=3").toOption.get)
      for {
        resp <- Main.routes.runZIO(req)
        body <- resp.body.asString
      } yield assertTrue(body.contains("Array(-1)"))
    }
  ).provideSomeLayer(appLayer) @@ parallel
}
