package com.example

object Main:

  private val pocs: Map[String, () => Unit] = Map(
    "actor"       -> ActorPoc.run,
    "stream"      -> StreamPoc.run,
    "http"        -> HttpPoc.run,
    "persistence" -> PersistencePoc.run,
    "cluster"     -> ClusterPoc.run,
    "sharding"    -> ShardingPoc.run,
    "ddata"       -> DistributedDataPoc.run
  )

  def main(args: Array[String]): Unit =
    val choice = args.headOption.getOrElse("actor").toLowerCase

    if choice == "all" then
      pocs.foreach { case (name, run) =>
        banner(name); run(); banner(s"$name done")
      }
    else
      pocs.get(choice) match
        case Some(run) =>
          banner(choice); run(); banner(s"$choice done")
        case None =>
          println(s"Unknown POC '$choice'.")
          println(s"Available: ${pocs.keys.toSeq.sorted.mkString(", ")}, all")

  private def banner(label: String): Unit =
    println(s"\n=== $label ===")
