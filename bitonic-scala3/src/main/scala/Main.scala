import zio.*
import zio.http.*
import zio.redis.*
import zio.schema.*
import zio.schema.codec.{BinaryCodec, ProtobufCodec}

object Main extends ZIOAppDefault {
  private val greetRoute =
    Method.GET / "greet" -> handler { (req: Request) =>
      ZIO.logInfo(s"Called /greet with queryParams=${req.url.queryParams("name")}") *> {
        val name = req.queryOrElse[String]("name", "World")
        val key = s"greet:$name"
        (for {
          redis <- ZIO.service[Redis]
          cached <- redis.get(key).returning[String]
          _ <- ZIO.logInfo(s"Cache lookup for key '$key': ${cached.getOrElse("MISS")}")
          result <- cached match {
            case Some(g) => ZIO.succeed((g, true))
            case None =>
              val g = s"Hello $name!"
              redis.set(key, g, Some(5.minutes)).as((g, false))
          }
          (greeting, wasCached) = result
          _ <- ZIO.logInfo(s"Greeting: $greeting (cached: $wasCached)")
          resp = Response.text(greeting).addHeader("X-Cache", if (wasCached) "HIT" else "MISS")
        } yield resp)
          .catchAll(e => ZIO.fail(Response.text(s"Unexpected error: ${e.getMessage}").status(Status.InternalServerError)))
      }
    }

  private val healthRoute = Method.GET / "health-check" -> handler {
    ZIO.logInfo("Called /health-check") *> ZIO.succeed(Response.text("Ok"))
  }

  private val routes: Routes[Redis, Nothing] = Routes(
    healthRoute,
    greetRoute
  )

  private object ProtobufCodecSupplier extends CodecSupplier {
    def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
  }

  // Environment variables for Redis configuration
  private val redisHost = sys.env.getOrElse("REDIS_HOST", "localhost")
  private val redisPort = sys.env.get("REDIS_PORT").flatMap(_.toIntOption).getOrElse(6379)
  private val appPort = sys.env.get("APP_PORT").flatMap(_.toIntOption).getOrElse(8080)

  def run: ZIO[Any, Throwable, Nothing] =
    Server
      .serve(routes)
      .provide(
        Server.defaultWithPort(appPort),
        Redis.singleNode,
        ZLayer.succeed[RedisConfig](RedisConfig(host = redisHost, port = redisPort)),
        ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier)
      )
}