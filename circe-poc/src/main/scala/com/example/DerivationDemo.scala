package com.example

import io.circe.{Decoder, Encoder, Json}
import io.circe.parser.parse
import io.circe.syntax.*

import java.util.UUID

object DerivationDemo:

  def run(): Unit =
    val person = Person(
      id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
      name = "Ada",
      age = 36,
      email = Some("ada@example.com"),
      tags = List("math", "engineering"),
      address = Address("1 Analytical St", "London", "SW1A")
    )

    // 1) Auto derivation — encoders/decoders materialized on demand at the call site.
    locally:
      import io.circe.generic.auto.*
      val json: Json = person.asJson
      println("auto encode:")
      println(json.spaces2)
      val decoded = parse(json.noSpaces).flatMap(_.as[Person])
      println(s"auto decode: $decoded")

    // 2) Semiauto derivation — explicit givens, recommended for stable compile times
    //    and to keep the typeclass instances visible/under your control.
    locally:
      given Encoder[Address] = io.circe.generic.semiauto.deriveEncoder
      given Decoder[Address] = io.circe.generic.semiauto.deriveDecoder
      given Encoder[Person]  = io.circe.generic.semiauto.deriveEncoder
      given Decoder[Person]  = io.circe.generic.semiauto.deriveDecoder

      val json = person.asJson
      println("\nsemiauto encode:")
      println(json.spaces2)
      val decoded = json.as[Person]
      println(s"semiauto decode: $decoded")

    // 3) `derives Codec.AsObject` — Scala 3 `derives` clause for a single combined codec.
    val widget = Widget(id = 7, label = "lever")
    println("\nderives Codec.AsObject encode:")
    println(widget.asJson.spaces2)
    val widgetBack = parse("""{"id":7,"label":"lever"}""").flatMap(_.as[Widget])
    println(s"derives Codec.AsObject decode: $widgetBack")

  final case class Widget(id: Int, label: String) derives io.circe.Codec.AsObject
