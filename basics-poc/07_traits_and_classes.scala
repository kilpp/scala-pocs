//> using scala 3.7.3

trait Animal:
  def name: String
  def sound: String
  def describe: String = s"$name says $sound"

trait Swimmer:
  def swim(): String = "swimming"

class Dog(val name: String) extends Animal:
  def sound: String = "woof"

class Fish(val name: String) extends Animal, Swimmer:
  def sound: String = "blub"

abstract class Shape:
  def area: Double
  def perimeter: Double

class Circle(radius: Double) extends Shape:
  def area: Double = math.Pi * radius * radius
  def perimeter: Double = 2 * math.Pi * radius

class Rectangle(w: Double, h: Double) extends Shape:
  def area: Double = w * h
  def perimeter: Double = 2 * (w + h)

@main def traitsAndClasses(): Unit =
  val d = Dog("Rex")
  println(d.describe)

  val f = Fish("Nemo")
  println(f.describe)
  println(f.swim())

  val shapes: List[Shape] = List(Circle(5), Rectangle(3, 4))
  shapes.foreach(s => println(s"area=${s.area}, perimeter=${s.perimeter}"))
