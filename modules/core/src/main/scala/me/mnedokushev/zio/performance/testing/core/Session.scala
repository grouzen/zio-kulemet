package me.mnedokushev.zio.performance.testing.core

import zio._

trait Session {

  protected val state: FiberRef[Session.State]

  def get: UIO[Map[String, String]] =
    state.get

  def get(key: String): UIO[Option[String]] =
    state.get.map(_.get(key))

  def set(key: String, value: String): UIO[Unit] =
    state.update(_.updated(key, value))

}

object Session {

  type State = Map[String, String]

  trait SimpleSession extends Session

  object SimpleSession {

    def layer: ULayer[SimpleSession] =
      ZLayer.scoped {
        FiberRef.make(Map.empty[String, String]).map { initialState =>
          new SimpleSession {
            override protected val state: FiberRef[State] = initialState
          }
        }
      }

  }

  trait FeederSession[A] extends SimpleSession {
    val feed: A
  }

  object FeederSession {

    def layer[A: Tag](initialFeed: A): ULayer[FeederSession[A]] =
      ZLayer.scoped {
        FiberRef.make(Map.empty[String, String]).map { initialState =>
          new FeederSession[A] {
            override val feed: A                          = initialFeed
            override protected val state: FiberRef[State] = initialState
          }
        }
      }

  }

}
