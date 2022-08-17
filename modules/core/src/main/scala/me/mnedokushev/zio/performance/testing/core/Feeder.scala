package me.mnedokushev.zio.performance.testing.core

import zio.ZIO
import zio.stream._

trait Feeder[A, B] {

  val input: A

  def feed: ZStream[Any, String, B]

}

case class FeederZio[B](input: ZIO[Any, Nothing, B]) extends Feeder[ZIO[Any, Nothing, B], B] {
  override def feed: ZStream[Any, String, B] =
    ZStream.fromZIO(input)
}
