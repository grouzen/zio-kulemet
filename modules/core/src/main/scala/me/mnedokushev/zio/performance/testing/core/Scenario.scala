package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.Action._
import me.mnedokushev.zio.performance.testing.core.Session.{ FeederSession, SimpleSession }
import zio._
import zio.stream._

object Scenario {

  def apply(name: String): SimpleScenario =
    SimpleScenario(name, ZIO.unit)

  def apply[A, B: Tag](name: String, feeder: Feeder[A, B]): FeederScenario[A, B] =
    FeederScenario(name, ZIO.unit, feeder)

}

abstract class Scenario[S: Tag] {

  protected val name: String

  protected val chain: ZIO[S, Throwable, Unit]

  def run: ZStream[ZState[Session.State], String, Unit]

  def exec[O](action: ExecAction[S, O]): Scenario[S] =
    addAction(action)

  def pause(action: PauseAction[S]): Scenario[S] =
    addAction(action)

  protected def nextChain[O](action: Action[S, O]): ZIO[S, Throwable, Unit] =
    chain *> action.run.unit

  protected def addAction[O](action: Action[S, O]): Scenario[S]

}

case class SimpleScenario(name: String, chain: ZIO[SimpleSession, Throwable, Unit]) extends Scenario[SimpleSession] {

  override protected def addAction[O](action: Action[SimpleSession, O]): Scenario[SimpleSession] =
    this.copy(chain = nextChain(action))

  override def run: ZStream[ZState[Session.State], String, Unit] =
    ZStream.fromZIO(chain).mapError(_.getMessage).provideLayer(SimpleSession.layer)

  def feeder: SimpleScenario.FeederPartiallyApplied =
    new SimpleScenario.FeederPartiallyApplied(name, chain)

}

object SimpleScenario {

  final class FeederPartiallyApplied(name: String, chain: ZIO[SimpleSession, Throwable, Unit]) {
    def apply[A, B: Tag](feeder: Feeder[A, B]): FeederScenario[A, B] =
      new FeederScenario[A, B](name, chain, feeder)
  }

}

case class FeederScenario[A, B: Tag](
  name: String,
  chain: ZIO[FeederSession[B], Throwable, Unit],
  feeder: Feeder[A, B]
) extends Scenario[FeederSession[B]] {

  override protected def addAction[O](action: Action[FeederSession[B], O]): Scenario[FeederSession[B]] =
    this.copy(chain = nextChain(action))

  override def run: ZStream[ZState[Session.State], String, Unit] =
    feeder.feed.flatMap { feed =>
      ZStream
        .fromZIO(chain)
        .mapError(_.getMessage)
        .provideLayer(FeederSession.layer(feed))
    }

}
