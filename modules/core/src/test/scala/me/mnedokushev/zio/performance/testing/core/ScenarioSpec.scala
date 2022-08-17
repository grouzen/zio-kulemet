package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.Action.{ ExecAction, FeederExecAction, SimpleExecAction }
import me.mnedokushev.zio.performance.testing.core.Session.{ FeederSession, SimpleSession }
import zio._
import zio.test._

object ScenarioSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ScenarioSpec")(
      test("Copy value from feeder to session state") {
        val scn =
          Scenario("test")
            .feeder(FeederZio(ZIO.succeed(1)))
            .exec(
              FeederExecAction[Int, Int]((_, b) => ZIO.succeed(b))
                .check(Checker((s, a) => s.set("foo", a.toString).as(true)))
            )

        ZIO.stateful(Map.empty[String, String]) {
          for {
            state   <- ZIO.service[ZState[Session.State]]
            _       <- scn.run.drain.runCollect
            session <- state.get
            result   = session.get("foo")
          } yield assertTrue(result.contains("1"))
        }
      },
      test("Modify session via check") {
        val scn = Scenario("test")
          .exec(
            SimpleExecAction[Int](_ => ZIO.succeed(1))
              .check(Checker((s, a) => s.set("foo", a.toString).as(true)))
          )

        ZIO.stateful(Map.empty[String, String]) {
          for {
            session <- ZIO.service[ZState[Session.State]]
            _       <- scn.run.drain.runCollect
            state   <- session.get
            result   = state.get("foo")
          } yield assertTrue(result.contains("1"))
        }
      },
      test("Use the value set by checker in the next exec action") {
        val scn = Scenario("test")
          .exec(
            SimpleExecAction[Int](_ => ZIO.succeed(1))
              .check(Checker((s, a) => s.set("foo", a.toString).as(true)))
          )
          .exec(
            SimpleExecAction[String](s => ZIO.succeed(s.getOrElse("foo", "bar")))
              .check(Checker((s, a) => s.set("foo1", a.toString).as(true)))
          )

        ZIO.stateful(Map.empty[String, String]) {
          for {
            session <- ZIO.service[ZState[Session.State]]
            _       <- scn.run.drain.runCollect
            state   <- session.get
            result   = state.get("foo1")
          } yield assertTrue(result.contains("1"))
        }
      }
    )

}
