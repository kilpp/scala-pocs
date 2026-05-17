package com.example

object ScalazPoc:

  def main(args: Array[String]): Unit =
    val demos: List[(String, () => Unit)] = List(
      "typeclasses"        -> TypeclassesDemo.run,
      "data-types"         -> DataTypesDemo.run,
      "validation"         -> ValidationDemo.run,
      "state-reader-writer" -> StateReaderWriterDemo.run,
      "monad-transformers" -> MonadTransformersDemo.run,
      "free"               -> FreeMonadDemo.run,
      "lenses"             -> LensesDemo.run,
      "io"                 -> IODemo.run,
      "tagged-memo"        -> TaggedAndMemoDemo.run
    )

    val selected =
      if args.isEmpty then demos
      else
        val asked = args.toSet
        demos.filter((name, _) => asked(name))

    selected.foreach { case (name, runDemo) =>
      println(s"\n────── $name ──────")
      runDemo()
    }
