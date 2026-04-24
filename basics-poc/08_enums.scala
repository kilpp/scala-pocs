//> using scala 3.7.3

enum Color:
  case Red, Green, Blue

enum Direction(val dx: Int, val dy: Int):
  case North extends Direction(0, 1)
  case South extends Direction(0, -1)
  case East  extends Direction(1, 0)
  case West  extends Direction(-1, 0)

enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)
  case Triangle(base: Double, height: Double)

def area(s: Shape): Double = s match
  case Shape.Circle(r)       => math.Pi * r * r
  case Shape.Rectangle(w, h) => w * h
  case Shape.Triangle(b, h)  => 0.5 * b * h

enum Tree[+A]:
  case Leaf
  case Node(value: A, left: Tree[A], right: Tree[A])

def size[A](t: Tree[A]): Int = t match
  case Tree.Leaf             => 0
  case Tree.Node(_, l, r)    => 1 + size(l) + size(r)

@main def enums(): Unit =
  println(Color.values.toList)
  println(Color.valueOf("Green"))

  val dirs = Direction.values.toList
  dirs.foreach(d => println(s"$d -> (${d.dx}, ${d.dy})"))

  val shapes: List[Shape] = List(Shape.Circle(5), Shape.Rectangle(3, 4), Shape.Triangle(6, 2))
  shapes.foreach(s => println(s"$s -> area=${area(s)}"))

  val tree: Tree[Int] =
    Tree.Node(1, Tree.Node(2, Tree.Leaf, Tree.Leaf), Tree.Node(3, Tree.Leaf, Tree.Leaf))
  println(s"tree size = ${size(tree)}")
