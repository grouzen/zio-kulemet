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
            .exec(
              FeederExecAction[Int, Int]((_, b) => ZIO.succeed(b))
                .check(Checker((s, a) => s.get("foo").map(r => r.contains(a.toString))))
            )

        for {
          stats    <- ZIO.service[Stats]
          _        <- scn.run.runDrain
          counters <- stats.getCounters
        } yield assertTrue(counters.ok == 2)
      }.provideLayer(Stats.layer),
      test("Modify session via check") {
        val scn = Scenario("test")
          .exec(
            SimpleExecAction[Int](_ => ZIO.succeed(1))
              .check(Checker((s, a) => s.set("foo", a.toString).as(true)))
          )

        for {
          stats    <- ZIO.service[Stats]
          _        <- scn.run.runDrain
          counters <- stats.getCounters
        } yield assertTrue(counters.ok == 1)
      }.provideLayer(Stats.layer),
      test("Use the value set by checker in the next exec action") {
        val scn = Scenario("test")
          .exec(
            SimpleExecAction[String](_ => ZIO.succeed("cat"))
              .check(Checker((s, a) => s.set("foo", a).as(true)))
          )
          .exec(
            SimpleExecAction[String](s => ZIO.succeed(s.getOrElse("foo", "dog")))
              .check(Checker((_, a) => ZIO.succeed(a == "cat")))
          )

        for {
          stats    <- ZIO.service[Stats]
          _        <- scn.run.runDrain
          counters <- stats.getCounters
        } yield assertTrue(counters.ok == 2)
      }.provideLayer(Stats.layer),
      test("Session should not be shared between virtual users") {
        val scn = Scenario("test")
          .feeder(new FiniteFeeder(Some(List("cat", "dog", "fox"))))
          .exec(
            FeederExecAction[String, String]((_, a) => ZIO.succeed(a))
              .check(Checker { case (s, a) =>
                for {
                  prev <- s.get("key")
                  value = prev.getOrElse("") + a
                  _    <- s.set("key", value)
                } yield value == a // it means that prev was not set
              })
          )

        for {
          stats    <- ZIO.service[Stats]
          _        <- scn.run.runDrain
          counters <- stats.getCounters
        } yield assertTrue(counters.ok == 3)
      }.provideLayer(Stats.layer)
    )

}
