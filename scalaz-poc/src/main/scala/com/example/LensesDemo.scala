package com.example

import scalaz.*

object LensesDemo:

  final case class Street(name: String, number: Int)
  final case class Address(city: String, street: Street)
  final case class Person(name: String, address: Address)

  private val addressL: Lens[Person, Address] =
    Lens.lensu[Person, Address]((p, a) => p.copy(address = a), _.address)

  private val streetL: Lens[Address, Street] =
    Lens.lensu[Address, Street]((a, s) => a.copy(street = s), _.street)

  private val numberL: Lens[Street, Int] =
    Lens.lensu[Street, Int]((s, n) => s.copy(number = n), _.number)

  private val cityL: Lens[Address, String] =
    Lens.lensu[Address, String]((a, c) => a.copy(city = c), _.city)

  // Compose lenses to drill all the way down.
  private val personStreetNumberL: Lens[Person, Int] =
    addressL >=> streetL >=> numberL

  def run(): Unit =
    println("=== Lenses ===")

    val ada = Person(
      "Ada",
      Address("London", Street("Marylebone", 12))
    )

    val moved          = personStreetNumberL.set(ada, 99)
    val incrementedNum = personStreetNumberL.mod(_ + 1, ada)
    val renamedCity    = (addressL >=> cityL).set(ada, "Cambridge")

    println(s"original          -> $ada")
    println(s"get number        -> ${personStreetNumberL.get(ada)}")
    println(s"set number = 99   -> $moved")
    println(s"mod number + 1    -> $incrementedNum")
    println(s"rename city       -> $renamedCity")
