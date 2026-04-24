//> using scala 3.7.3

@main def collections(): Unit =
  val list = List(1, 2, 3, 4, 5)
  println(list.head)
  println(list.tail)
  println(0 :: list)
  println(list ++ List(6, 7))

  val vec = Vector("a", "b", "c")
  println(vec(1))
  println(vec.updated(0, "A"))

  val set = Set(1, 2, 2, 3, 3, 3)
  println(set)
  println(set.contains(2))

  val map = Map("one" -> 1, "two" -> 2, "three" -> 3)
  println(map("two"))
  println(map.get("four"))
  println(map + ("four" -> 4))

  val range = (1 to 10).toList
  println(range)
  println(range.filter(_ % 2 == 0))
  println(range.map(_ * 10))
  println(range.foldLeft(0)(_ + _))
  println(range.groupBy(_ % 3))

  val tup: (Int, String, Boolean) = (1, "two", true)
  println(tup._2)
  val (a, b, c) = tup
  println(s"$a $b $c")
