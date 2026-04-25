//> using scala 3.7.3

def divmod(a: Int, b: Int): (Int, Int) = (a / b, a % b)

def stats(xs: List[Int]): (Int, Int, Double) =
  (xs.min, xs.max, xs.sum.toDouble / xs.size)

@main def tuples(): Unit =
  val pair: (Int, String) = (1, "one")
  println(pair._1)
  println(pair._2)

  val triple = (1, 2.0, "three")
  println(triple)

  val (q, r) = divmod(17, 5)
  println(s"17 / 5 = $q rem $r")

  val (lo, hi, avg) = stats(List(3, 1, 4, 1, 5, 9, 2, 6))
  println(s"min=$lo max=$hi avg=$avg")

  val swapped = pair.swap
  println(swapped)

  val zipped = List(1, 2, 3).zip(List("a", "b", "c"))
  println(zipped)
  zipped.foreach((n, s) => println(s"$n -> $s"))

  val withIndex = List("a", "b", "c").zipWithIndex
  println(withIndex)
