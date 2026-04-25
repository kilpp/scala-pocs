//> using scala 3.7.3

class Box[A](val value: A):
  def map[B](f: A => B): Box[B] = Box(f(value))
  override def toString: String = s"Box($value)"

def first[A](xs: List[A]): Option[A] = xs.headOption

def pair[A, B](a: A, b: B): (A, B) = (a, b)

class Stack[A]:
  private var items: List[A] = Nil
  def push(a: A): Unit = items = a :: items
  def pop(): Option[A] = items match
    case h :: t => items = t; Some(h)
    case Nil    => None
  def peek: Option[A] = items.headOption
  def size: Int = items.size

trait Container[+A]:
  def get: A
class Cell[A](a: A) extends Container[A]:
  def get: A = a

def maxOf[A](xs: List[A])(using ord: Ordering[A]): A =
  xs.reduce((x, y) => if ord.gt(x, y) then x else y)

@main def generics(): Unit =
  val box = Box(42).map(_ * 2).map(n => s"value=$n")
  println(box)

  println(first(List(1, 2, 3)))
  println(first(List.empty[String]))

  println(pair(1, "one"))

  val s = Stack[Int]()
  s.push(1); s.push(2); s.push(3)
  println(s.peek)
  println(s.pop())
  println(s.size)

  val c: Container[Any] = Cell[String]("hello")
  println(c.get)

  println(maxOf(List(3, 1, 4, 1, 5, 9, 2, 6)))
  println(maxOf(List("pear", "apple", "banana")))
