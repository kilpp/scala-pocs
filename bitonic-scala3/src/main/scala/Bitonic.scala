import zio.{UIO, ULayer, ZIO, ZLayer}

import scala.collection.mutable

trait Bitonic:
  def bitonicArray(n: Int, l: Int, r: Int): UIO[Array[Int]]

object Bitonic:

  private final case class Impl() extends Bitonic:
    def bitonicArray(n: Int, l: Int, r: Int): UIO[Array[Int]] =
      ZIO.succeed {
        if n > (r - l) * 2 + 1 then
          Array(-1)
        else
          val dq = mutable.ArrayDeque[Int]()
          dq.append(r - 1)
          var i = r
          while i >= l && dq.size < n do
            dq.append(i)
            i -= 1
          i = r - 2
          while i >= l && dq.size < n do
            dq.prepend(i)
            i -= 1
          dq.toArray
      }

  val live: ULayer[Bitonic] = ZLayer.succeed(Impl())