//> using scala 3.7.3

sealed trait Json
case class JsonString(value: String) extends Json
case class JsonNumber(value: Double) extends Json
case class JsonBool(value: Boolean) extends Json
case class JsonArray(items: List[Json]) extends Json
case class JsonObject(fields: Map[String, Json]) extends Json
case object JsonNull extends Json

def render(j: Json): String = j match
  case JsonString(s)  => s"\"$s\""
  case JsonNumber(n)  => n.toString
  case JsonBool(b)    => b.toString
  case JsonArray(xs)  => xs.map(render).mkString("[", ", ", "]")
  case JsonObject(fs) => fs.map((k, v) => s"\"$k\": ${render(v)}").mkString("{", ", ", "}")
  case JsonNull       => "null"

sealed trait Result[+A]
case class Ok[A](value: A) extends Result[A]
case class Err(msg: String) extends Result[Nothing]

@main def sealedTraits(): Unit =
  val doc = JsonObject(Map(
    "name"   -> JsonString("Ada"),
    "age"    -> JsonNumber(36),
    "admin"  -> JsonBool(true),
    "tags"   -> JsonArray(List(JsonString("dev"), JsonString("ops"))),
    "spouse" -> JsonNull
  ))
  println(render(doc))

  val results: List[Result[Int]] = List(Ok(1), Err("nope"), Ok(42))
  results.foreach:
    case Ok(v)    => println(s"ok: $v")
    case Err(msg) => println(s"err: $msg")
