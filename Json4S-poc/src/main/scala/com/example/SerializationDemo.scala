package com.example

import org.json4s.*
import org.json4s.native.Serialization.{read, write, writePretty}

object SerializationDemo:

  // DefaultFormats covers primitives, Option, List, Map, BigInt/BigDecimal.
  given Formats = DefaultFormats

  def run(): Unit =
    val ada = Person(
      name    = "Ada Lovelace",
      age     = 36,
      email   = Some("ada@example.com"),
      tags    = List("math", "cs", "poetry"),
      address = Address("1 Analytical Way", "London", "SW1")
    )
    val anon = ada.copy(name = "Anonymous", email = None, tags = Nil)

    println("--- ada ---")
    println(writePretty(ada))
    println("--- anonymous (None + empty list) ---")
    println(writePretty(anon))

    // read[T] needs a Manifest — synthesized automatically for case classes.
    val backAda  = read[Person](write(ada))
    val backAnon = read[Person](write(anon))
    println(s"ada  round-trip equal? ${ada  == backAda}")
    println(s"anon round-trip equal? ${anon == backAnon}")

    // Explicit `null` on an Option field deserializes to None.
    val withNull = """{"name":"x","age":1,"email":null,"tags":[],
                     |"address":{"street":"s","city":"c","zip":"z"}}""".stripMargin
    println(s"null email → ${read[Person](withNull).email}")
