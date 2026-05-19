package com.example

object CircePoc:

  def main(args: Array[String]): Unit =
    section("Auto vs semiauto vs derives Codec.AsObject")(DerivationDemo.run())
    section("Configured ADT (discriminator + snake_case + defaults)")(ConfiguredAdtDemo.run())

  private def section(title: String)(body: => Unit): Unit =
    println()
    println("=" * 60)
    println(title)
    println("=" * 60)
    body
