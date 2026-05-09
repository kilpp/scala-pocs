package com.example

import org.json4s.*
import org.json4s.JsonDSL.*
import org.json4s.native.JsonMethods.*

object AstDemo:

  def run(): Unit =
    // Build JSON with the DSL — `~` chains fields, tuples become JFields,
    // Scala literals/collections lift via implicit conversions in JsonDSL.
    val built: JValue =
      ("name" -> "Ada") ~
        ("age"     -> 36) ~
        ("tags"    -> List("math", "cs")) ~
        ("address" -> (("street" -> "1 Analytical Way") ~ ("city" -> "London"))) ~
        ("active"  -> true) ~
        ("score"   -> 3.14)

    println("DSL-built (pretty):")
    println(pretty(render(built)))
    println(s"DSL-built (compact): ${compact(render(built))}")

    // Parse JSON text → AST
    val parsed = parse("""{"a":1,"b":[2,3,4],"c":{"d":"x","e":null}}""")
    println(s"\nparsed compact: ${compact(render(parsed))}")

    // Path navigation with `\`
    println(s"a       = ${parsed \ "a"}")
    println(s"b[0]    = ${(parsed \ "b")(0)}")
    println(s"c.d     = ${parsed \ "c" \ "d"}")
    println(s"missing = ${parsed \ "nope"} (a.k.a. JNothing)")

    // Deep search with `\\` collects fields by name at any depth
    val nested = parse("""{"x":1,"y":{"x":2,"z":{"x":3}}}""")
    println(s"\nall x   = ${nested \\ "x"}")

    // findField — first match
    val firstBigX = nested.findField {
      case JField("x", JInt(n)) => n > 1
      case _                    => false
    }
    println(s"first x>1 = $firstBigX")

    // filterField — all matches
    val bigXs = nested.filterField {
      case JField("x", JInt(n)) => n > 1
      case _                    => false
    }
    println(s"x>1 list = $bigXs")

    // values: convert AST → Scala primitives (Map / List / Int / ...)
    println(s"as values = ${parsed.values}")

    // Construct AST directly without the DSL
    val direct: JValue = JObject(
      JField("ok", JBool(true)),
      JField("xs", JArray(List(JInt(1), JInt(2)))),
      JField("nope", JNull)
    )
    println(s"\ndirect AST: ${compact(render(direct))}")
