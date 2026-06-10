package com.example

import java.nio.file.{Files, Path}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.meta.*

/** A class-like declaration (class, trait, object, or enum) found in the sources. */
final case class Decl(name: String, kind: String, file: String, line: Int)

/** Analysis result: every declaration found and the subset that is never referenced. */
final case class Report(declared: List[Decl], unused: List[Decl]):
  def used: List[Decl] = declared.filterNot(unused.toSet)

/** Finds classes, traits, objects, and enums that no live code references.
  *
  * The analysis is purely syntactic (scalameta parse, no compilation), so
  * declarations are matched to references by simple name: two same-named
  * classes in different packages are treated as one entity, and an import of
  * a name counts as a reference. Removal is transitive — a class referenced
  * only from other unused classes is itself reported unused — but mutually
  * recursive dead cycles survive, since each member keeps the other alive.
  * Objects extending App are entry points and always count as used.
  */
object UnusedClassDetector:

  // One definition tree's source range. References that fall inside a range of
  // their own name (self-references, companion members) or inside a range of
  // an already-dead name do not keep a declaration alive.
  private final case class Site(decl: Decl, start: Int, end: Int, entryPoint: Boolean)

  private final case class Ref(name: String, file: String, start: Int)

  def analyzeDirectory(dir: Path): Report =
    val sources = Files.walk(dir).iterator().asScala
      .filter(p => Files.isRegularFile(p) && p.toString.endsWith(".scala"))
      .map(p => dir.relativize(p).toString -> Files.readString(p))
      .toMap
    analyzeSources(sources)

  def analyzeSources(sources: Map[String, String]): Report =
    val parsed: List[(String, Source)] = sources.toList.sortBy(_._1).map: (file, code) =>
      dialects.Scala3(code).parse[Source] match
        case Parsed.Success(tree) => file -> tree
        case error: Parsed.Error  => throw IllegalArgumentException(s"$file: ${error.message}")

    val collected = parsed.flatMap((file, tree) => collectSites(file, tree))
    val sites = collected.map(_._1)
    // The name token of a definition is not a reference to it.
    val definingTokens = collected.map((site, nameStart) => (site.decl.file, nameStart)).toSet

    val sitesByName: Map[String, List[Site]] = sites.groupBy(_.decl.name)
    val declaredNames = sitesByName.keySet

    val refsByName: Map[String, List[Ref]] = parsed
      .flatMap: (file, tree) =>
        tree.collect:
          case n: Name if declaredNames(n.value) && !definingTokens((file, n.pos.start)) =>
            Ref(n.value, file, n.pos.start)
      .groupBy(_.name)

    @tailrec
    def grow(unused: Set[String]): Set[String] =
      val deadRanges = unused.toList.flatMap(sitesByName)
      def alive(name: String): Boolean =
        sitesByName(name).exists(_.entryPoint) ||
          refsByName.getOrElse(name, Nil).exists: ref =>
            !within(ref, sitesByName(name)) && !within(ref, deadRanges)
      val next = declaredNames.filterNot(alive)
      if next == unused then unused else grow(next)

    val unusedNames = grow(Set.empty)
    val declared = sites.map(_.decl).sortBy(d => (d.file, d.line))
    Report(declared, declared.filter(d => unusedNames(d.name)))

  private def within(ref: Ref, sites: List[Site]): Boolean =
    sites.exists(s => s.decl.file == ref.file && ref.start >= s.start && ref.start < s.end)

  private def collectSites(file: String, tree: Source): List[(Site, Int)] =
    def site(defn: Tree, name: Name, kind: String, entryPoint: Boolean = false) =
      val decl = Decl(name.value, kind, file, name.pos.startLine + 1)
      (Site(decl, defn.pos.start, defn.pos.end, entryPoint), name.pos.start)
    tree.collect:
      case d: Defn.Class  => site(d, d.name, "class")
      case d: Defn.Trait  => site(d, d.name, "trait")
      case d: Defn.Object => site(d, d.name, "object", entryPoint = extendsApp(d.templ))
      case d: Defn.Enum   => site(d, d.name, "enum")

  private def extendsApp(templ: Template): Boolean =
    templ.inits.exists(_.tpe.syntax == "App")
