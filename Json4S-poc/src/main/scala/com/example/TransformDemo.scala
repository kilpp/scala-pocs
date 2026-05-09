package com.example

import org.json4s.*
import org.json4s.native.JsonMethods.*

object TransformDemo:

  def run(): Unit =
    val a = parse("""{"name":"Ada","age":36,"tags":["math","cs"]}""")
    val b = parse("""{"age":37,"email":"ada@example.com","tags":["poetry"]}""")

    // merge: b overlays a; arrays concat at the same path.
    val merged = a merge b
    println(s"merge   = ${compact(render(merged))}")

    // diff: reports changed / added / deleted between two ASTs.
    val Diff(changed, added, deleted) = a.diff(b)
    println(s"changed = ${compact(render(changed))}")
    println(s"added   = ${compact(render(added))}")
    println(s"deleted = ${compact(render(deleted))}")

    // transformField — rewrite matching fields (recursively).
    val upper = a.transformField {
      case ("name", JString(n)) => ("name", JString(n.toUpperCase))
    }
    println(s"upper   = ${compact(render(upper))}")

    // mapField — like map but for JFields; non-matching pass through.
    val agePlus1 = a.mapField {
      case ("age", JInt(n)) => ("age", JInt(n + 1))
      case other            => other
    }
    println(s"age+1   = ${compact(render(agePlus1))}")

    // removeField — drop matching fields.
    val noTags = a.removeField {
      case ("tags", _) => true
      case _           => false
    }
    println(s"no tags = ${compact(render(noTags))}")

    // replace — walk a path, replace the leaf JValue.
    val replaced = a.replace(List("name"), JString("Augusta"))
    println(s"replace = ${compact(render(replaced))}")
