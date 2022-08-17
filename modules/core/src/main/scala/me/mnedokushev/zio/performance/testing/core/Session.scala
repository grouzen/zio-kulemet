package me.mnedokushev.zio.performance.testing.core

import zio._

trait Session {

  protected val state: ZState[Session.State]

  def get: UIO[Map[String, String]] =
    state.get

  def get(key: String): UIO[Option[String]] =
    state.get.map(_.get(key))

  def set(key: String, value: String): UIO[Unit] =
    for {
      current <- state.get
      updated  = current.updated(key, value)
      _       <- state.set(updated)
    } yield ()

}

object Session {

  type State = Map[String, String]

  trait SimpleSession extends Session

  object SimpleSession {

    def layer: URLayer[ZState[State], SimpleSession] =
      ZLayer {
        ZIO.serviceWith[ZState[State]] { initialState =>
          new SimpleSession {
            override protected val state: ZState[State] = initialState
          }
        }
      }

  }

  trait FeederSession[A] extends SimpleSession {
    val feed: A
  }

  object FeederSession {

    def layer[A: Tag](initialFeed: A): URLayer[ZState[State], FeederSession[A]] =
      ZLayer {
        ZIO.serviceWith[ZState[State]] { initialState =>
          new FeederSession[A] {
            override val feed: A                        = initialFeed
            override protected val state: ZState[State] = initialState
          }
        }
      }

  }

}
