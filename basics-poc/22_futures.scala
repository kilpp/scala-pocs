//> using scala 3.7.3

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.{Success, Failure}

def slowDouble(n: Int): Future[Int] = Future:
  Thread.sleep(50)
  n * 2

def fetch(url: String): Future[String] = Future:
  Thread.sleep(20)
  s"<html>$url</html>"

@main def futures(): Unit =
  val f = slowDouble(21)
  val result = Await.result(f, 1.second)
  println(s"awaited: $result")

  val mapped = slowDouble(5).map(_ + 1)
  println(Await.result(mapped, 1.second))

  val composed =
    for
      a <- slowDouble(2)
      b <- slowDouble(3)
      c <- slowDouble(a + b)
    yield c
  println(Await.result(composed, 1.second))

  val all = Future.sequence(List(slowDouble(1), slowDouble(2), slowDouble(3)))
  println(Await.result(all, 1.second))

  val pages = List("a", "b", "c").map(fetch)
  val joined = Future.sequence(pages).map(_.mkString(" | "))
  println(Await.result(joined, 1.second))

  val recovered = Future[Int](throw RuntimeException("boom")).recover:
    case _: RuntimeException => -1
  println(Await.result(recovered, 1.second))

  Await.ready(slowDouble(7).andThen {
    case Success(v) => println(s"callback got $v")
    case Failure(e) => println(s"callback err $e")
  }, 1.second)
