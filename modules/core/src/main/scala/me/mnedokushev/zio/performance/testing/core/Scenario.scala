package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.Action._
import me.mnedokushev.zio.performance.testing.core.Session.{ FeederSession, SimpleSession, State }
import zio._
import zio.stream._

abstract class Scenario[S: Tag] {

  def run: ZStream[Stats, String, Unit]

  def exec[O](action: ExecAction[S, O]): Scenario[S] =
    addAction(action)

  def pause(action: PauseAction[S]): Scenario[S] =
    addAction(action)

  protected val name: String

  protected val actions: ZIO[S with Stats, Throwable, Unit]

  protected def nextAction[O](action: Action[S, O]): ZIO[S with Stats, Throwable, Unit] =
    actions *> action.run.unit

  protected def addAction[O](action: Action[S, O]): Scenario[S]

}

object Scenario {

  def apply(name: String): SimpleScenario =
    SimpleScenario(name, ZIO.unit)

}

case class SimpleScenario(name: String, actions: ZIO[SimpleSession with Stats, Throwable, Unit])
    extends Scenario[SimpleSession] {

  override def run: ZStream[Stats, String, Unit] =
    ZStream
      .fromZIO(actions.provideSomeLayer[Stats](SimpleSession.layer))
      .mapError(_.getMessage)

  def feeder: SimpleScenario.FeederPartiallyApplied =
    new SimpleScenario.FeederPartiallyApplied(name, actions)

  override protected def addAction[O](action: Action[SimpleSession, O]): Scenario[SimpleSession] =
    this.copy(actions = nextAction(action))

}

object SimpleScenario {

  final class FeederPartiallyApplied(name: String, actions: ZIO[SimpleSession with Stats, Throwable, Unit]) {
    def apply[B: Tag](feeder: Feeder[B]): FeederScenario[B] =
      new FeederScenario[B](name, actions, feeder)
  }

}

case class FeederScenario[B: Tag](
  name: String,
  actions: ZIO[FeederSession[B] with Stats, Throwable, Unit],
  feeder: Feeder[B]
) extends Scenario[FeederSession[B]] {

  override def run: ZStream[Stats, String, Unit] =
    for {
      feed <- feeder.feed
      _    <- ZStream
                .fromZIO(actions.provideSomeLayer[Stats](FeederSession.layer(feed)))
                .mapError(_.getMessage)
    } yield ()

//  override def run: ZStream[ZState[Session.State], String, Unit] =
//    feeder.feed
//      .mapZIOParUnordered(5)(feed =>
//        actions
//          .provideLayer(FeederSession.layer(feed))
//          .mapError(_.getMessage)
//      )
//      .haltAfter(10.seconds)

  override protected def addAction[O](action: Action[FeederSession[B], O]): Scenario[FeederSession[B]] =
    this.copy(actions = nextAction(action))

}
