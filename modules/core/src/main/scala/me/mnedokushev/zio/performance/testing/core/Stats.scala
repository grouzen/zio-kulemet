package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.Stats.Counters
import zio._

trait Stats {

  def incOK: UIO[Unit]

  def incKO: UIO[Unit]

  def getCounters: UIO[Counters]

}

object Stats {

  case class Counters(ok: Long = 0, ko: Long = 0)

  def layer: ULayer[Stats] =
    ZLayer.scoped(FiberRef.make(Counters()).map { countersRef =>
      new Stats {
        override def incOK: UIO[Unit] =
          countersRef.update(c => c.copy(ok = c.ok + 1))

        override def incKO: UIO[Unit] =
          countersRef.update(c => c.copy(ko = c.ko + 1))

        override def getCounters: UIO[Counters] =
          countersRef.get
      }
    })

}
