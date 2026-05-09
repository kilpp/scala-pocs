package com.example

import org.json4s.*
import org.json4s.JsonDSL.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization.{read, write}

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

final class Json4sPocSpec extends AnyFunSuite, Matchers:

  given Formats = DefaultFormats

  test("DSL builds a JObject with mixed types"):
    val ast: JValue = ("a" -> 1) ~ ("b" -> List(2, 3)) ~ ("c" -> true)
    compact(render(ast)) shouldBe """{"a":1,"b":[2,3],"c":true}"""

  test("read/write round-trips a case class with Some + non-empty List"):
    val p = Person("Ada", 36, Some("ada@example.com"), List("a", "b"),
                   Address("s", "c", "z"))
    read[Person](write(p)) shouldBe p

  test("None + empty list round-trip back unchanged"):
    val p = Person("Anon", 0, None, Nil, Address("s", "c", "z"))
    read[Person](write(p)) shouldBe p

  test("explicit JSON null on an Option field deserializes to None"):
    val json =
      """{"name":"x","age":1,"email":null,"tags":[],
        |"address":{"street":"s","city":"c","zip":"z"}}""".stripMargin
    val p = read[Person](json)
    p.email shouldBe None
    p.tags  shouldBe Nil

  test("\\ navigation extracts nested values; missing keys are JNothing"):
    val p   = Person("N", 1, Some("e"), List("t1", "t2"), Address("s", "c", "z"))
    val ast = parse(write(p))
    (ast \ "address" \ "city").extract[String]    shouldBe "c"
    (ast \ "tags").extract[List[String]]          shouldBe List("t1", "t2")
    (ast \ "missing")                             shouldBe JNothing

  test("\\\\ deep-search collects fields with the same name at any depth"):
    val ast = parse("""{"x":1,"y":{"x":2,"z":{"x":3}}}""")
    val collected = ast \\ "x" match
      case JObject(fields) => fields.collect { case (_, JInt(n)) => n.toInt }
      case _               => Nil
    collected should contain theSameElementsAs List(1, 2, 3)

  test("merge layers two objects and concatenates arrays"):
    val a = parse("""{"a":1,"b":{"c":2},"xs":[1]}""")
    val b = parse("""{"b":{"d":3},"e":4,"xs":[2]}""")
    val m = a merge b
    (m \ "b" \ "c").extract[Int]      shouldBe 2
    (m \ "b" \ "d").extract[Int]      shouldBe 3
    (m \ "e").extract[Int]            shouldBe 4
    (m \ "xs").extract[List[Int]]     shouldBe List(1, 2)

  test("diff classifies changes as changed / added / deleted"):
    val a = parse("""{"a":1,"b":2,"d":9}""")
    val b = parse("""{"a":1,"b":3,"c":4}""")
    val Diff(changed, added, deleted) = a diff b
    (changed \ "b").extract[Int] shouldBe 3
    (added   \ "c").extract[Int] shouldBe 4
    (deleted \ "d").extract[Int] shouldBe 9

  test("transformField rewrites matching fields recursively"):
    val ast = parse("""{"name":"ada","nested":{"name":"bob"}}""")
    val upper = ast.transformField {
      case ("name", JString(n)) => ("name", JString(n.toUpperCase))
    }
    (upper \ "name").extract[String]              shouldBe "ADA"
    (upper \ "nested" \ "name").extract[String]   shouldBe "BOB"

  test("type hints round-trip a sealed-trait hierarchy"):
    given Formats =
      org.json4s.native.Serialization.formats(
        ShortTypeHints(List(classOf[Dog], classOf[Cat], classOf[Fish]))
      )
    val zoo  = Zoo(List(Dog("Rex", "Lab"), Cat("W", indoor = true), Fish("N", "salt")))
    val json = write(zoo)
    json should include("\"jsonClass\":\"Dog\"")
    read[Zoo](json) shouldBe zoo
