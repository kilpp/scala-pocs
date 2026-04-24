//> using scala 3.7.3

case class Point(x: Int, y: Int):
  def distanceTo(other: Point): Double =
    val dx = (x - other.x).toDouble
    val dy = (y - other.y).toDouble
    math.sqrt(dx * dx + dy * dy)

case class Person(name: String, age: Int)

@main def caseClasses(): Unit =
  val p1 = Point(0, 0)
  val p2 = Point(3, 4)
  println(p1)
  println(s"distance = ${p1.distanceTo(p2)}")

  val p3 = p2.copy(y = 10)
  println(p3)

  println(p1 == Point(0, 0))

  val Point(px, py) = p2
  println(s"extracted: x=$px, y=$py")

  val ada = Person("Ada", 36)
  val older = ada.copy(age = ada.age + 1)
  println(s"$ada -> $older")

  val people = List(Person("Ada", 36), Person("Bob", 25), Person("Cleo", 40))
  println(people.sortBy(_.age))
  println(people.maxBy(_.age))
