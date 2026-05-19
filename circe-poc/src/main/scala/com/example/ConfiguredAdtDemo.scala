package com.example

import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.circe.parser.parse
import io.circe.syntax.*

import java.time.Instant
import java.util.UUID

// Configuration is shared across the ADT so encode and decode agree on the
// discriminator, key renaming, and default handling. Without `useDefaults`,
// missing fields in the JSON would fail decoding even when a Scala default exists.
object ConfiguredAdtDemo:

  given Configuration = Configuration.default
    .withSnakeCaseMemberNames
    .withDiscriminator("type")
    .withDefaults

  // ConfiguredCodec derived on the sealed parent flows down to each child case.
  given ConfiguredCodec[Event]              = ConfiguredCodec.derived
  given ConfiguredCodec[Event.UserCreated]  = ConfiguredCodec.derived
  given ConfiguredCodec[Event.UserRenamed]  = ConfiguredCodec.derived
  given ConfiguredCodec[Event.UserDeleted]  = ConfiguredCodec.derived

  def run(): Unit =
    val events: List[Event] = List(
      Event.UserCreated(
        eventId     = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001"),
        occurredAt  = Instant.parse("2026-05-19T10:00:00Z"),
        userId      = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001"),
        displayName = "ada",
        isAdmin     = true
      ),
      Event.UserRenamed(
        eventId      = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002"),
        occurredAt   = Instant.parse("2026-05-19T10:05:00Z"),
        userId       = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001"),
        previousName = "ada",
        newName      = "ada.l"
      ),
      Event.UserDeleted(
        eventId    = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003"),
        occurredAt = Instant.parse("2026-05-19T10:10:00Z"),
        userId     = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001")
      )
    )

    println("encoded ADT with discriminator + snake_case keys:")
    println(events.asJson.spaces2)

    // Decode that includes a field with no `reason` — `withDefaults` lets
    // `reason` fall back to its Scala default `None` instead of failing.
    val deletedJson =
      """{
        |  "type": "UserDeleted",
        |  "event_id": "aaaaaaaa-0000-0000-0000-000000000099",
        |  "occurred_at": "2026-05-19T11:00:00Z",
        |  "user_id": "bbbbbbbb-0000-0000-0000-000000000001"
        |}""".stripMargin

    val decoded = parse(deletedJson).flatMap(_.as[Event])
    println(s"\ndecoded with default reason=None: $decoded")
