//> using scala 3.7.3

object MathUtils:
  val Pi: Double = 3.14159
  def square(x: Int): Int = x * x
  def cube(x: Int): Int = x * x * x

class Circle(val radius: Double):
  def area: Double = MathUtils.Pi * radius * radius

object Circle:
  def unit: Circle = Circle(1.0)
  def fromDiameter(d: Double): Circle = Circle(d / 2)

case object EmptyConfig:
  val name: String = "empty"

case class Config(host: String, port: Int)

@main def objects(): Unit =
  println(MathUtils.Pi)
  println(MathUtils.square(5))
  println(MathUtils.cube(3))

  val c = Circle.unit
  println(s"unit area = ${c.area}")
  println(Circle.fromDiameter(10).area)

  println(EmptyConfig)
  println(EmptyConfig.name)

  val cfg: Any = Config("localhost", 8080)
  cfg match
    case EmptyConfig          => println("empty")
    case Config(host, port)   => println(s"$host:$port")
