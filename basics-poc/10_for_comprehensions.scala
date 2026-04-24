//> using scala 3.7.3

@main def forComprehensions(): Unit =
  val pairs =
    for
      x <- List(1, 2, 3)
      y <- List("a", "b")
    yield (x, y)
  println(pairs)

  val evens =
    for
      n <- 1 to 10
      if n % 2 == 0
    yield n
  println(evens.toList)

  val pythag =
    for
      a <- 1 to 20
      b <- a to 20
      c = math.sqrt(a * a + b * b)
      if c == c.toInt
    yield (a, b, c.toInt)
  println(pythag.toList)

  val result: Option[Int] =
    for
      a <- Some(10)
      b <- Some(20)
      c <- Some(30)
    yield a + b + c
  println(result)

  val fail: Option[Int] =
    for
      a <- Some(10)
      b <- None
      c <- Some(30)
    yield a + b + c
  println(fail)

  val eitherResult: Either[String, Int] =
    for
      a <- Right(1)
      b <- Right(2)
      c <- Right(3)
    yield a + b + c
  println(eitherResult)
