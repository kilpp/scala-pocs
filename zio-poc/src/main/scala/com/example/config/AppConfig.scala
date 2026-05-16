package com.example.config

import zio.*
import zio.config.magnolia.*
import zio.config.typesafe.*

final case class HttpConfig(host: String, port: Int)
final case class DbConfig(url: String, driver: String, user: String, password: String)
final case class BatchConfig(chunkSize: Int)
final case class AppConfig(name: String, http: HttpConfig, db: DbConfig, batch: BatchConfig)

object AppConfig:
  // zio-config 4.x: deriveConfig returns a ZIO Config[A]; TypesafeConfigProvider reads HOCON
  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer {
      TypesafeConfigProvider
        .fromResourcePath()
        .load(deriveConfig[AppConfig].nested("app"))
    }
