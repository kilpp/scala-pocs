package com.example

import com.google.gson.{Gson, GsonBuilder}

object GsonPoc:

  // Single configured instance — register Scala-aware adapters once and reuse.
  // Gson instantiates case classes via Unsafe (no no-arg ctor), so `apply`
  // logic and field defaults declared on the constructor are skipped on read.
  val gson: Gson =
    GsonBuilder()
      .registerTypeAdapterFactory(OptionTypeAdapterFactory())
      .registerTypeAdapterFactory(ScalaListTypeAdapterFactory())
      .serializeNulls()
      .setPrettyPrinting()
      .create()

  def main(args: Array[String]): Unit =
    val ada = Person(
      name = "Ada Lovelace",
      age = 36,
      email = Some("ada@example.com"),
      tags = List("math", "cs", "poetry"),
      address = Address("1 Analytical Way", "London", "SW1")
    )
    val anon = ada.copy(name = "Anonymous", email = None, tags = Nil)

    val jsonAda  = gson.toJson(ada)
    val jsonAnon = gson.toJson(anon)
    println("--- ada ---")
    println(jsonAda)
    println("--- anonymous (None + empty list) ---")
    println(jsonAnon)

    val backAda  = gson.fromJson(jsonAda, classOf[Person])
    val backAnon = gson.fromJson(jsonAnon, classOf[Person])
    println("--- round-trip ---")
    println(s"ada  == back: ${ada == backAda}")
    println(s"anon == back: ${anon == backAnon}")
