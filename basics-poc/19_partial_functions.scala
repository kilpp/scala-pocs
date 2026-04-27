//> using scala 3.7.3

val divide: PartialFunction[(Int, Int), Int] =
  case (a, b) if b != 0 => a / b

val classify: PartialFunction[Int, String] =
  case 0          => "zero"
  case n if n > 0 => "positive"

val negative: PartialFunction[Int, String] =
  case n if n < 0 => "negative"

@main def partialFunctions(): Unit =
  println(divide.isDefinedAt((10, 2)))
  println(divide.isDefinedAt((10, 0)))
  println(divide((10, 2)))

  val safe = divide.lift
  println(safe((10, 2)))
  println(safe((10, 0)))

  val full = classify.orElse(negative).orElse { case _ => "unknown" }
  println(full(0))
  println(full(5))
  println(full(-7))

  val xs = List(1, "two", 3, "four", 5)
  val ints = xs.collect { case i: Int => i * 10 }
  println(ints)

  val mapped = (1 to 5).toList.collect:
    case n if n % 2 == 0 => s"even:$n"
  println(mapped)
