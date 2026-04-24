//> using scala 3.7.3

extension (s: String)
  def shout: String = s.toUpperCase + "!"
  def reverseWords: String = s.split(" ").reverse.mkString(" ")

extension (n: Int)
  def squared: Int = n * n
  def times(body: => Unit): Unit =
    var i = 0
    while i < n do
      body
      i += 1

extension [A](xs: List[A])
  def second: Option[A] = xs.drop(1).headOption
  def penultimate: Option[A] = xs.dropRight(1).lastOption

@main def extensionMethods(): Unit =
  println("hello".shout)
  println("the quick brown fox".reverseWords)

  println(5.squared)
  3.times { println("hi") }

  val xs = List(10, 20, 30, 40)
  println(xs.second)
  println(xs.penultimate)
  println(List.empty[Int].second)
