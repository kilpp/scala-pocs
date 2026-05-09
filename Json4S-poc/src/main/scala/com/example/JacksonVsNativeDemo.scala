package com.example

import org.json4s.*
import org.json4s.native.{JsonMethods => NativeJ, Serialization => NativeS}
import org.json4s.jackson.{JsonMethods => JacksonJ, Serialization => JacksonS}

object JacksonVsNativeDemo:

  // Same Formats works for both backends — only the parser/printer differs.
  given Formats = DefaultFormats

  def run(): Unit =
    val p = Person(
      "Ada", 36, Some("ada@example.com"),
      List("math", "cs"), Address("1", "London", "SW1")
    )

    val nativeOut  = NativeS.writePretty(p)
    val jacksonOut = JacksonS.writePretty(p)
    println("--- native backend ---")
    println(nativeOut)
    println("--- jackson backend ---")
    println(jacksonOut)

    // Cross-parse: each backend reads the other's output.
    val p1 = JacksonS.read[Person](nativeOut)
    val p2 = NativeS.read[Person](jacksonOut)
    println(s"\nnative→jackson read equal? ${p1 == p}")
    println(s"jackson→native read equal? ${p2 == p}")

    // The AST is shared; only render/parse implementations change.
    val ast1 = NativeJ.parse(nativeOut)
    val ast2 = JacksonJ.parse(jacksonOut)
    println(s"ASTs equal across backends? ${ast1 == ast2}")
