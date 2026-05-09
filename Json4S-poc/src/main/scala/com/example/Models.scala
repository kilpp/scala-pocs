package com.example

import java.time.LocalDate
import java.util.UUID

case class Address(street: String, city: String, zip: String)

case class Person(
    name: String,
    age: Int,
    email: Option[String],
    tags: List[String],
    address: Address
)

// CustomFormatsDemo
final case class Money(amountCents: Long, currency: String)
final case class Account(id: UUID, owner: String, balance: Money, opened: LocalDate)

// TypeHintsDemo — sealed trait hierarchy serialized with a discriminator field
sealed trait Animal
final case class Dog(name: String, breed: String)      extends Animal
final case class Cat(name: String, indoor: Boolean)    extends Animal
final case class Fish(name: String, waterType: String) extends Animal

final case class Zoo(animals: List[Animal])
