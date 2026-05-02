package com.example

case class Address(street: String, city: String, zip: String)

case class Person(
    name: String,
    age: Int,
    email: Option[String],
    tags: List[String],
    address: Address
)
