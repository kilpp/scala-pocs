package com.example

import java.time.Instant
import java.util.UUID

// DerivationDemo — plain case classes for auto vs semiauto comparison.
final case class Address(street: String, city: String, zip: String)

final case class Person(
    id: UUID,
    name: String,
    age: Int,
    email: Option[String],
    tags: List[String],
    address: Address
)

// ConfiguredAdtDemo — sealed ADT with member fields exercising defaults,
// snake_case key renaming, and a "type" discriminator on encode/decode.
sealed trait Event:
  def eventId: UUID
  def occurredAt: Instant

object Event:
  final case class UserCreated(
      eventId: UUID,
      occurredAt: Instant,
      userId: UUID,
      displayName: String,
      isAdmin: Boolean = false
  ) extends Event

  final case class UserRenamed(
      eventId: UUID,
      occurredAt: Instant,
      userId: UUID,
      previousName: String,
      newName: String
  ) extends Event

  final case class UserDeleted(
      eventId: UUID,
      occurredAt: Instant,
      userId: UUID,
      reason: Option[String] = None
  ) extends Event
