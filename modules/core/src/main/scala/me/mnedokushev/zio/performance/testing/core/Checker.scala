package me.mnedokushev.zio.performance.testing.core

import zio._

case class Checker[S: Tag, A](body: (S, A) => UIO[Boolean]) {

  def check(actionResult: A): URIO[S with Stats, Unit] =
    for {
      state  <- ZIO.service[S]
      stats  <- ZIO.service[Stats]
      result <- body(state, actionResult)
      _      <- if (result) stats.incOK else stats.incKO
    } yield ()

}
