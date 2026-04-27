//> using scala 3.7.3

import scala.collection.mutable

@main def mutableCollections(): Unit =
  val buf = mutable.ArrayBuffer(1, 2, 3)
  buf += 4
  buf ++= List(5, 6)
  buf.prepend(0)
  println(buf)
  buf(0) = 100
  println(buf)

  val m = mutable.Map("one" -> 1, "two" -> 2)
  m("three") = 3
  m += ("four" -> 4)
  m -= "one"
  println(m)

  val s = mutable.Set[Int]()
  s += 1; s += 2; s += 2; s += 3
  println(s)
  s -= 2
  println(s)

  val q = mutable.Queue[String]()
  q.enqueue("a"); q.enqueue("b"); q.enqueue("c")
  println(q.dequeue())
  println(q)

  val st = mutable.Stack[Int]()
  st.push(1); st.push(2); st.push(3)
  println(st.pop())
  println(st)

  val immutable = buf.toList
  println(immutable)

  val frozen = m.toMap
  println(frozen)
