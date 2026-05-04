package com.example

import org.springframework.data.annotation.{
  CreatedBy, CreatedDate, Id, LastModifiedBy, LastModifiedDate, Version
}
import org.springframework.data.jdbc.core.mapping.AggregateReference
import org.springframework.data.relational.core.mapping.{Embedded, MappedCollection, Table}

import java.time.Instant
import scala.annotation.meta.field
import scala.jdk.CollectionConverters.*

// ── Child entities (no @Id — they're value entities owned by Person) ────────────

@Table("address")
final case class Address(street: String, city: String, zip: String)

@Table("phone")
final case class Phone(number: String)

@Table("hobby")
final case class Hobby(name: String)

// ── @Embedded value object — flattens into people table with prefix "contact_" ─

final case class ContactInfo(phone: String, website: String)

// ── Aggregate root ─────────────────────────────────────────────────────────────

// Every Spring Data annotation needs `@(X @field)` so the annotation lands on
// the JVM field where Spring's reflection looks for it.
//
// Per-field notes:
//   • id        — nullable Long; Spring detects "is new" via id == null
//   • email     — Email value type; ↔ VARCHAR via custom converters
//   • department— AggregateReference; stores only the FK id, not the full row
//   • addresses — Set child collection (java.util.Set, not scala.collection.Set)
//   • phones    — Map child collection; `keyColumn` holds the map key
//   • hobbies   — List child collection; `keyColumn` holds the list index
//   • contact   — @Embedded.Nullable; columns prefixed contact_, null if all null
//   • version   — @Version; null on insert (set to 0), incremented on update
//   • createdAt et al — @CreatedDate/@CreatedBy/etc. populated by auditing
@Table("people")
final case class Person(
    @(Id @field) id: java.lang.Long,
    name: String,
    age: Int,
    email: Email,
    department: AggregateReference[Department, java.lang.Long],
    @(MappedCollection @field)(idColumn = "person")
    addresses: java.util.Set[Address],
    @(MappedCollection @field)(idColumn = "person", keyColumn = "label")
    phones: java.util.Map[String, Phone],
    @(MappedCollection @field)(idColumn = "person", keyColumn = "position")
    hobbies: java.util.List[Hobby],
    @(Embedded.Nullable @field)(prefix = "contact_")
    contact: ContactInfo,
    @(Version @field)          version:        java.lang.Long,
    @(CreatedDate @field)      createdAt:      Instant,
    @(LastModifiedDate @field) lastModifiedAt: Instant,
    @(CreatedBy @field)        createdBy:      String,
    @(LastModifiedBy @field)   lastModifiedBy: String
)

object Person:

  def newPerson(
      name:       String,
      age:        Int,
      email:      Email = null,
      department: AggregateReference[Department, java.lang.Long] = null,
      addresses:  Set[Address]            = Set.empty,
      phones:     Map[String, Phone]      = Map.empty,
      hobbies:    List[Hobby]             = List.empty,
      contact:    ContactInfo             = null
  ): Person =
    Person(
      id             = null,
      name           = name,
      age            = age,
      email          = email,
      department     = department,
      addresses      = addresses.asJava,
      phones         = phones.asJava,
      hobbies        = hobbies.asJava,
      contact        = contact,
      version        = null,
      createdAt      = null,
      lastModifiedAt = null,
      createdBy      = null,
      lastModifiedBy = null
    )

  extension (p: Person)
    def addressesScala: Set[Address]        = p.addresses.asScala.toSet
    def phonesScala:    Map[String, Phone]  = p.phones.asScala.toMap
    def hobbiesScala:   List[Hobby]         = p.hobbies.asScala.toList
