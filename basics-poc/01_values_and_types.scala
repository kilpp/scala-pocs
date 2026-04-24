//> using scala 3.7.3

@main def valuesAndTypes(): Unit =
  val name: String = "Ada"
  val age: Int = 36
  val pi: Double = 3.14159
  val active: Boolean = true
  val letter: Char = 'A'
  val big: Long = 10_000_000_000L

  var counter: Int = 0
  counter += 1

  val inferred = "type is inferred as String"

  val greeting = s"Hello, $name! You are $age years old."
  val math = s"2 + 2 = ${2 + 2}"
  val raw = raw"No \n escaping here"
  val multi =
    """line one
      |line two
      |line three""".stripMargin

  println(greeting)
  println(math)
  println(raw)
  println(multi)
  println(s"counter=$counter, pi=$pi, active=$active, letter=$letter, big=$big, inferred=$inferred")
