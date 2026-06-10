package com.example

import java.nio.file.Files

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

final class UnusedClassDetectorSpec extends AnyFunSuite, Matchers:

  private def unusedNames(sources: Map[String, String]): Set[String] =
    UnusedClassDetector.analyzeSources(sources).unused.map(_.name).toSet

  test("flags a class that nothing references"):
    val sources = Map(
      "A.scala" ->
        """class Used
          |class Dead
          |object Main extends App:
          |  val u = new Used
          |""".stripMargin
    )
    unusedNames(sources) shouldBe Set("Dead")

  test("a reference from another file counts as usage"):
    val sources = Map(
      "Model.scala"   -> "class Account",
      "Service.scala" ->
        """object Billing extends App:
          |  def open(): Account = new Account
          |""".stripMargin
    )
    unusedNames(sources) shouldBe empty

  test("companion object members do not keep their own class alive"):
    val sources = Map(
      "Foo.scala" ->
        """class Foo
          |object Foo:
          |  def make: Foo = new Foo
          |""".stripMargin
    )
    val report = UnusedClassDetector.analyzeSources(sources)
    report.unused.map(d => (d.kind, d.name)) should contain theSameElementsAs
      List(("class", "Foo"), ("object", "Foo"))

  test("self-references do not keep a class alive"):
    val sources = Map(
      "Node.scala" ->
        """class Node:
          |  def next: Node = this
          |""".stripMargin
    )
    unusedNames(sources) shouldBe Set("Node")

  test("classes referenced only from unused classes are transitively unused"):
    val sources = Map(
      "Legacy.scala" ->
        """class Repo
          |class Service(r: Repo)
          |""".stripMargin,
      "Main.scala" -> "object Main extends App:\n  println(\"hi\")"
    )
    unusedNames(sources) shouldBe Set("Repo", "Service")

  test("the same chain stays alive when the entry point uses its head"):
    val sources = Map(
      "Legacy.scala" ->
        """class Repo
          |class Service(r: Repo)
          |""".stripMargin,
      "Main.scala" ->
        """object Main extends App:
          |  println(new Service(new Repo))
          |""".stripMargin
    )
    unusedNames(sources) shouldBe empty

  test("a class that is only imported is still flagged unused"):
    val sources = Map(
      "Dead.scala" ->
        """package legacy
          |class Dead
          |""".stripMargin,
      "Main.scala" ->
        """import legacy.Dead
          |object Main extends App:
          |  println("never touches Dead")
          |""".stripMargin
    )
    unusedNames(sources) shouldBe Set("Dead")

  test("an imported class actually used in the body stays alive"):
    val sources = Map(
      "Dead.scala" ->
        """package legacy
          |class Dead
          |""".stripMargin,
      "Main.scala" ->
        """import legacy.Dead
          |object Main extends App:
          |  println(new Dead)
          |""".stripMargin
    )
    unusedNames(sources) shouldBe empty

  test("an import used only inside the importing class's own body still respects self-reference rules"):
    val sources = Map(
      "Helper.scala" ->
        """package legacy
          |class Helper
          |""".stripMargin,
      "Orphan.scala" ->
        """import legacy.Helper
          |class Orphan:
          |  def helper: Helper = new Helper
          |""".stripMargin,
      "Main.scala" -> "object Main extends App:\n  println(\"hi\")"
    )
    // Orphan is dead, so its body usages cannot keep Helper alive either.
    unusedNames(sources) shouldBe Set("Helper", "Orphan")

  test("rename imports resolve alias usages to the original class"):
    val sources = Map(
      "Helper.scala" ->
        """package legacy
          |class Helper
          |""".stripMargin,
      "Main.scala" ->
        """import legacy.Helper as H
          |object Main extends App:
          |  println(new H)
          |""".stripMargin
    )
    unusedNames(sources) shouldBe empty

  test("a rename import whose alias is never used does not keep the original alive"):
    val sources = Map(
      "Helper.scala" ->
        """package legacy
          |class Helper
          |""".stripMargin,
      "Main.scala" ->
        """import legacy.Helper as H
          |object Main extends App:
          |  println("never touches H")
          |""".stripMargin
    )
    unusedNames(sources) shouldBe Set("Helper")

  test("mutually recursive dead classes survive (documented limitation)"):
    val sources = Map(
      "Cycle.scala" ->
        """class Ping(p: Pong)
          |class Pong(p: Ping)
          |""".stripMargin
    )
    unusedNames(sources) shouldBe empty

  test("traits, enums, and objects are detected with kind and position"):
    val sources = Map(
      "Kinds.scala" ->
        """trait Plugin
          |enum Color:
          |  case Red, Green
          |object Registry
          |""".stripMargin
    )
    val report = UnusedClassDetector.analyzeSources(sources)
    report.declared.map(d => (d.kind, d.name, d.line)) should contain theSameElementsAs
      List(("trait", "Plugin", 1), ("enum", "Color", 2), ("object", "Registry", 4))
    report.unused should contain theSameElementsAs report.declared
    report.used shouldBe empty

  test("analyzeDirectory walks nested directories and ignores non-Scala files"):
    val dir = Files.createTempDirectory("ucd-poc")
    Files.createDirectories(dir.resolve("nested"))
    Files.writeString(dir.resolve("Model.scala"), "class Order\nclass Draft")
    Files.writeString(
      dir.resolve("nested").resolve("Main.scala"),
      "object Main extends App:\n  println(new Order)"
    )
    Files.writeString(dir.resolve("notes.txt"), "class NotScala")

    val report = UnusedClassDetector.analyzeDirectory(dir)
    report.declared.map(_.name) should contain theSameElementsAs List("Order", "Draft", "Main")
    report.unused.map(_.name) shouldBe List("Draft")

  test("an unparseable source fails loudly with the file name"):
    val ex = intercept[IllegalArgumentException]:
      UnusedClassDetector.analyzeSources(Map("Broken.scala" -> "class {{{"))
    ex.getMessage should include("Broken.scala")
