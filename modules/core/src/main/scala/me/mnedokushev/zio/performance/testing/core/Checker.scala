package me.mnedokushev.zio.performance.testing.core

import zio._

case class Checker[S: Tag, A](body: (S, A) => UIO[Boolean]) {

  // TODO: check the result of body and update requests stats
  def check(result: A): URIO[S, Unit] =
    ZIO.serviceWithZIO[S](s => body(s, result).unit)

}
