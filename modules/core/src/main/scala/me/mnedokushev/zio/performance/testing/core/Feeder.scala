package me.mnedokushev.zio.performance.testing.core

import zio.stream._

trait Feeder[A, R, B] {

  val input: A

  def feed: ZStream[R, String, B]

}
