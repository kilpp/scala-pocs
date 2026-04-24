//> using scala 3.7.3

@main def patternMatching(): Unit =
  def describe(x: Any): String = x match
    case 0               => "zero"
    case i: Int if i < 0 => s"negative int $i"
    case i: Int          => s"positive int $i"
    case s: String       => s"string of length ${s.length}"
    case (a, b)          => s"pair($a, $b)"
    case List(1, _*)     => "list starting with 1"
    case Nil             => "empty list"
    case _               => "something else"

  println(describe(0))
  println(describe(-7))
  println(describe(42))
  println(describe("hello"))
  println(describe((1, 2)))
  println(describe(List(1, 2, 3)))
  println(describe(Nil))
  println(describe(3.14))

  val list = List(1, 2, 3, 4)
  list match
    case head :: tail => println(s"head=$head, tail=$tail")
    case Nil          => println("empty")

  val (x, y) = (10, 20)
  println(s"destructured: $x, $y")
