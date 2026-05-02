package com.example

import com.google.gson.reflect.TypeToken
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

final class GsonPocSpec extends AnyFunSuite, Matchers:

  private val gson = GsonPoc.gson

  test("round-trips a case class with Option(Some) and a non-empty List"):
    val p = Person("Ada", 36, Some("ada@example.com"), List("a", "b"), Address("s", "c", "z"))
    val back = gson.fromJson(gson.toJson(p), classOf[Person])
    back shouldBe p

  test("None serializes as null and round-trips back to None"):
    val p = Person("Anon", 0, None, Nil, Address("s", "c", "z"))
    val json = gson.toJson(p)
    json should include("\"email\": null")
    val back = gson.fromJson(json, classOf[Person])
    back shouldBe p

  test("explicit JSON null on Option field deserializes to None"):
    val json =
      """{"name":"x","age":1,"email":null,"tags":[],"address":{"street":"s","city":"c","zip":"z"}}"""
    val p = gson.fromJson(json, classOf[Person])
    p.email shouldBe None
    p.tags shouldBe Nil

  test("top-level Scala List round-trips via TypeToken"):
    val tpe  = new TypeToken[List[Int]] {}.getType
    val xs   = List(1, 2, 3, 4)
    val json = gson.toJson(xs, tpe)
    json should not include "$"   // not falling through to bean serialization
    val back: List[Int] = gson.fromJson(json, tpe)
    back shouldBe xs

  test("nested object equality after round-trip"):
    val p = Person("N", 1, Some("e"), List("t"), Address("s", "c", "z"))
    val back = gson.fromJson(gson.toJson(p), classOf[Person])
    back.address shouldBe p.address
