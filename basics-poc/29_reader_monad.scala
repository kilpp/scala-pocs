//> using scala 3.7.3

final case class Reader[R, A](run: R => A):
  def map[B](f: A => B): Reader[R, B] = Reader(r => f(run(r)))
  def flatMap[B](f: A => Reader[R, B]): Reader[R, B] = Reader(r => f(run(r)).run(r))

object Reader:
  def pure[R, A](a: A): Reader[R, A] = Reader(_ => a)
  def ask[R]: Reader[R, R] = Reader(identity)
  def asks[R, A](f: R => A): Reader[R, A] = Reader(f)

case class Config(host: String, port: Int, prefix: String)

def url(path: String): Reader[Config, String] =
  for
    host <- Reader.asks[Config, String](_.host)
    port <- Reader.asks[Config, Int](_.port)
  yield s"http://$host:$port$path"

def labelled(path: String): Reader[Config, String] =
  for
    prefix <- Reader.asks[Config, String](_.prefix)
    full   <- url(path)
  yield s"$prefix=$full"

@main def readerMonad(): Unit =
  val cfg = Config("example.com", 8080, "url")
  println(url("/api/v1/users").run(cfg))
  println(labelled("/health").run(cfg))

  val cfg2 = Config("localhost", 9000, "local")
  println(labelled("/").run(cfg2))
