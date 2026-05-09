package com.example

import org.json4s.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, writePretty}

object TypeHintsDemo:

  // ShortTypeHints adds a `jsonClass` discriminator field carrying the
  // simple class name; FullTypeHints uses the FQN; MappedTypeHints lets you
  // pick the strings yourself.
  given Formats =
    Serialization.formats(ShortTypeHints(List(classOf[Dog], classOf[Cat], classOf[Fish])))

  def run(): Unit =
    val zoo = Zoo(List(
      Dog("Rex", "Lab"),
      Cat("Whiskers", indoor = true),
      Fish("Nemo", "salt")
    ))

    val json = writePretty(zoo)
    println(json)

    val back = read[Zoo](json)
    println(s"\nround-trip equal? ${back == zoo}")
