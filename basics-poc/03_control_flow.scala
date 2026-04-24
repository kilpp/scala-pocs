//> using scala 3.7.3

@main def controlFlow(): Unit =
  val x = 42

  val label =
    if x > 0 then "positive"
    else if x == 0 then "zero"
    else "negative"
  println(s"$x is $label")

  var i = 0
  while i < 3 do
    println(s"while i=$i")
    i += 1

  for n <- 1 to 3 do println(s"for n=$n")

  val squares = for n <- 1 to 5 yield n * n
  println(squares)

  val pairs =
    for
      a <- 1 to 3
      b <- 1 to 3
      if a != b
    yield (a, b)
  println(pairs)

  val day = 3
  val name = day match
    case 1 => "Mon"
    case 2 => "Tue"
    case 3 => "Wed"
    case _ => "other"
  println(name)
