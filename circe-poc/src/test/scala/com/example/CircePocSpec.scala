package com.example

import io.circe.{Decoder, Encoder}
import io.circe.parser.parse
import io.circe.syntax.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.util.UUID

class CircePocSpec extends AnyFunSuite with Matchers:

  private val person = Person(
    id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
    name = "Ada",
    age = 36,
    email = Some("ada@example.com"),
    tags = List("math", "engineering"),
    address = Address("1 Analytical St", "London", "SW1A")
  )

  test("auto derivation round-trips a case class"):
    import io.circe.generic.auto.*
    val json = person.asJson.noSpaces
    parse(json).flatMap(_.as[Person]) shouldBe Right(person)

  test("semiauto derivation round-trips a case class"):
    given Encoder[Address] = io.circe.generic.semiauto.deriveEncoder
    given Decoder[Address] = io.circe.generic.semiauto.deriveDecoder
    given Encoder[Person]  = io.circe.generic.semiauto.deriveEncoder
    given Decoder[Person]  = io.circe.generic.semiauto.deriveDecoder

    val json = person.asJson.noSpaces
    parse(json).flatMap(_.as[Person]) shouldBe Right(person)

  test("configured ADT encodes the discriminator and snake_case keys"):
    import ConfiguredAdtDemo.given

    val ev: Event = Event.UserCreated(
      eventId     = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001"),
      occurredAt  = Instant.parse("2026-05-19T10:00:00Z"),
      userId      = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001"),
      displayName = "ada",
      isAdmin     = true
    )
    val obj = ev.asJson.asObject.value

    obj("type").value.asString          shouldBe Some("UserCreated")
    obj("event_id").value.asString      shouldBe Some("aaaaaaaa-0000-0000-0000-000000000001")
    obj("display_name").value.asString  shouldBe Some("ada")
    obj("is_admin").value.asBoolean     shouldBe Some(true)
    obj.contains("eventId")             shouldBe false

  test("configured ADT round-trips through the sealed parent codec"):
    import ConfiguredAdtDemo.given

    val ev: Event = Event.UserRenamed(
      eventId      = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002"),
      occurredAt   = Instant.parse("2026-05-19T10:05:00Z"),
      userId       = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001"),
      previousName = "ada",
      newName      = "ada.l"
    )
    parse(ev.asJson.noSpaces).flatMap(_.as[Event]) shouldBe Right(ev)

  test("withDefaults lets missing optional fields fall back to their Scala default"):
    import ConfiguredAdtDemo.given

    val json =
      """{
        |  "type": "UserDeleted",
        |  "event_id": "aaaaaaaa-0000-0000-0000-000000000003",
        |  "occurred_at": "2026-05-19T10:10:00Z",
        |  "user_id": "bbbbbbbb-0000-0000-0000-000000000001"
        |}""".stripMargin

    val decoded = parse(json).flatMap(_.as[Event])
    decoded shouldBe Right(
      Event.UserDeleted(
        eventId    = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003"),
        occurredAt = Instant.parse("2026-05-19T10:10:00Z"),
        userId     = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001"),
        reason     = None
      )
    )

  extension [A](opt: Option[A])
    private def value: A = opt.getOrElse(fail("expected Some, got None"))
