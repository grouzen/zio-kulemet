package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.Session.{ FeederSession, SimpleSession }
import zio._

abstract class Action[S: Tag, A] { self =>

  def run: RIO[S, A]

}

object Action {

  abstract class ExecAction[S: Tag, A] extends Action[S, A] {
    val checker: Option[Checker[S, A]]
  }

  case class SimpleExecAction[A](body: Session.State => UIO[A], checker: Option[Checker[SimpleSession, A]] = None)
      extends ExecAction[SimpleSession, A] {

    def check(chkr: Checker[SimpleSession, A]): SimpleExecAction[A] =
      this.copy(checker = Some(chkr))

    override def run: RIO[SimpleSession, A] =
      for {
        session <- ZIO.service[SimpleSession]
        state   <- session.get
        result  <- body(state)
        _       <- ZIO.foreach(checker)(_.check(result))
      } yield result

  }

  case class FeederExecAction[B: Tag, A](
    body: (Session.State, B) => UIO[A],
    checker: Option[Checker[FeederSession[B], A]] = None
  ) extends ExecAction[FeederSession[B], A] {

    def check(chkr: Checker[FeederSession[B], A]): FeederExecAction[B, A] =
      this.copy(checker = Some(chkr))

    override def run: RIO[FeederSession[B], A] =
      for {
        session <- ZIO.service[FeederSession[B]]
        state   <- session.get
        result  <- body(state, session.feed)
        _       <- ZIO.foreach(checker)(_.check(result))
      } yield result

  }

  case class PauseAction[S: Tag](body: S => Duration) extends Action[S, Unit] {
    override def run: RIO[S, Unit] =
      ZIO.serviceWithZIO[S](s => ZIO.sleep(body(s)))
  }

}
