package com.example

import java.nio.file.Path

object UnusedClassDetectorPoc:

  // A tiny fake codebase analyzed by the demo run when no path is given:
  // Square is directly unused; LegacyService and LegacyRepo are dead together
  // (only the dead service references the repo); the rest hangs off Main.
  val sampleSources: Map[String, String] = Map(
    "Shapes.scala" ->
      """trait Shape:
        |  def area: Double
        |
        |final class Circle(r: Double) extends Shape:
        |  def area: Double = math.Pi * r * r
        |
        |final class Square(side: Double) extends Shape:
        |  def area: Double = side * side
        |""".stripMargin,
    "Legacy.scala" ->
      """final class LegacyRepo:
        |  def load(): String = "rows"
        |
        |final class LegacyService(repo: LegacyRepo):
        |  def run(): String = repo.load()
        |""".stripMargin,
    "Main.scala" ->
      """object Main extends App:
        |  val shape: Shape = Circle(2.0)
        |  println(shape.area)
        |""".stripMargin
  )

  def main(args: Array[String]): Unit =
    val report = args.headOption match
      case Some(dir) => UnusedClassDetector.analyzeDirectory(Path.of(dir))
      case None      => UnusedClassDetector.analyzeSources(sampleSources)

    println(s"${report.declared.size} declaration(s), ${report.unused.size} unused")
    println()
    val unused = report.unused.toSet
    report.declared.foreach: d =>
      val mark = if unused(d) then "UNUSED" else "used  "
      println(f"  $mark  ${d.kind}%-6s ${d.name}%-15s ${d.file}:${d.line}")
