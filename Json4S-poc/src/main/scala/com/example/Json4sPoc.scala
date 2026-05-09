package com.example

object Json4sPoc:

  def main(args: Array[String]): Unit =
    section("AST + JsonDSL")(AstDemo.run())
    section("Serialization (read / write)")(SerializationDemo.run())
    section("Transform / merge / diff")(TransformDemo.run())
    section("Custom serializers + FieldSerializer")(CustomFormatsDemo.run())
    section("Type hints (sealed-trait polymorphism)")(TypeHintsDemo.run())
    section("Jackson vs native backend")(JacksonVsNativeDemo.run())

  private def section(title: String)(body: => Unit): Unit =
    println()
    println("=" * 60)
    println(title)
    println("=" * 60)
    body
