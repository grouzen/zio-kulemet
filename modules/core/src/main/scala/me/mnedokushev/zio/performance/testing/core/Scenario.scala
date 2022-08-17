package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.Action._
import me.mnedokushev.zio.performance.testing.core.Session.{ FeederSession, SimpleSession }
import zio._
import zio.stream._

object Scenario {

  def apply(name: String): SimpleScenario[Unit] =
    SimpleScenario(name, Chain(ZIO.unit))

  def apply[A, B: Tag](name: String, feeder: Feeder[A, B]): FeederScenario[A, B, Unit] =
    FeederScenario(name, Chain(ZIO.unit), feeder)

  case class Chain[-S, O](materialized: ZIO[S, Throwable, Unit], last: Option[Action[S, O]] = None)

}

abstract class Scenario[S: Tag, U] {

  protected val name: String

  protected val chain: Scenario.Chain[S, U]

  def run: ZStream[ZState[Session.State], String, Unit]

  def exec[O](action: ExecAction[S, O]): Scenario[S, O] =
    addAction(action)

  def pause(action: PauseAction[S]): Scenario[S, Unit] =
    addAction(action)

  protected def nextChain[O](action: Action[S, O]): Scenario.Chain[S, O] =
    chain.last match {
      case Some(last) =>
        chain.copy(materialized = chain.materialized *> last.run.unit, last = Some(action))
      case _          =>
        chain.copy(last = Some(action))
    }

  protected def materializeChain: ZIO[S, Throwable, Unit] = chain.last match {
    case Some(last) =>
      chain.materialized *> last.run.unit
    case _          =>
      chain.materialized
  }

  protected def addAction[O](action: Action[S, O]): Scenario[S, O]

}

case class SimpleScenario[U](name: String, chain: Scenario.Chain[SimpleSession, U]) extends Scenario[SimpleSession, U] {

  override protected def addAction[O](action: Action[SimpleSession, O]): Scenario[SimpleSession, O] =
    this.copy(chain = nextChain(action))

  override def run: ZStream[ZState[Session.State], String, Unit] =
    ZStream.fromZIO(materializeChain).mapError(_.getMessage).provideLayer(SimpleSession.layer)

  def feeder: SimpleScenario.FeederPartiallyApplied[U] =
    new SimpleScenario.FeederPartiallyApplied(name, chain)

}

object SimpleScenario {

  final class FeederPartiallyApplied[U](name: String, chain: Scenario.Chain[SimpleSession, U]) {
    def apply[A, B: Tag](feeder: Feeder[A, B]): FeederScenario[A, B, U] =
      new FeederScenario[A, B, U](name, chain, feeder)
  }

}

case class FeederScenario[A, B: Tag, U](
  name: String,
  chain: Scenario.Chain[FeederSession[B], U],
  feeder: Feeder[A, B]
) extends Scenario[FeederSession[B], U] {

  override protected def addAction[O](action: Action[FeederSession[B], O]): Scenario[FeederSession[B], O] =
    this.copy(chain = nextChain(action))

  override def run: ZStream[ZState[Session.State], String, Unit] =
    feeder.feed.flatMap { feed =>
      ZStream
        .fromZIO(materializeChain)
        .mapError(_.getMessage)
        .provideLayer(FeederSession.layer(feed))
    }

}
