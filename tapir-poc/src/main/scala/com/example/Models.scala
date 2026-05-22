package com.example

import io.circe.Codec
import sttp.tapir.Schema

final case class Greeting(message: String) derives Codec.AsObject, Schema

final case class Task(
    id:    Long,
    title: String,
    done:  Boolean
) derives Codec.AsObject,
      Schema

final case class CreateTask(title: String) derives Codec.AsObject, Schema

final case class ErrorInfo(message: String) derives Codec.AsObject, Schema
