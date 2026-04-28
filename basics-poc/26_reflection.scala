//> using scala 3.7.3

import scala.reflect.ClassTag
import scala.reflect.TypeTest

case class Point(x: Int, y: Int)

def className[A](a: A): String = a.getClass.getName

def newArray[A: ClassTag](size: Int, fill: A): Array[A] =
  val arr = new Array[A](size)
  var i = 0
  while i < size do
    arr(i) = fill
    i += 1
  arr

def collectByType[A](xs: List[Any])(using tt: TypeTest[Any, A]): List[A] =
  xs.collect { case tt(a) => a }

@main def reflection(): Unit =
  println(className(42))
  println(className("hi"))
  println(className(Point(1, 2)))

  val ints = newArray[Int](3, 7)
  println(ints.toList)

  val strs = newArray[String](2, "x")
  println(strs.toList)

  val mixed: List[Any] = List(1, "two", 3, "four", 5.0, true)
  println(collectByType[Int](mixed))
  println(collectByType[String](mixed))

  val p = Point(3, 4)
  val mirror = p.productIterator.toList
  println(s"fields = $mirror")
  println(s"arity = ${p.productArity}")
  println(s"name = ${p.productPrefix}")
  p.productElementNames.zip(p.productIterator).foreach: (k, v) =>
    println(s"$k -> $v")
