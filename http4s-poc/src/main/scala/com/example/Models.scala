package com.example

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class Task(id: Long, title: String, done: Boolean)
object Task:
  given Encoder[Task] = deriveEncoder
  given Decoder[Task] = deriveDecoder

final case class CreateTask(title: String)
object CreateTask:
  given Decoder[CreateTask] = deriveDecoder
  given Encoder[CreateTask] = deriveEncoder

final case class Greeting(message: String)
object Greeting:
  given Encoder[Greeting] = deriveEncoder
  given Decoder[Greeting] = deriveDecoder
