import zio.*
import zio.redis.*
import zio.schema.*
import zio.schema.codec.*

object ZioRedis extends ZIOAppDefault {

  private object ProtobufCodecSupplier extends CodecSupplier {
    def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
  }

  private val myApp: ZIO[Redis, RedisError, Unit] =
    for {
      redis <- ZIO.service[Redis]
      _ <- redis.set("myKey", 8L, Some(1.minutes))
      v <- redis.get("myKey").returning[Long]
      _ <- Console.printLine(s"Value of myKey: $v").orDie
      _ <- redis.hSet("myHash", ("k1", 6), ("k2", 2))
      _ <- redis.rPush("myList", 1, 2, 3, 4)
      _ <- redis.sAdd("mySet", "a", "b", "a", "c")
    } yield ()

  override def run: ZIO[Any, RedisError, Unit] =
    myApp.provide(Redis.local, ZLayer.succeed[CodecSupplier](ProtobufCodecSupplier))
}