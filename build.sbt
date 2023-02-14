import BuildHelper._

inThisBuild(
  List(
    organization := "me.mnedokushev"
  )
)

lazy val root =
  project
    .in(file("."))
    .aggregate(runner, core, http, httpDefault, feederJsonDefault)
    .settings(publish / skip := true)

lazy val runner =
  project
    .in(file("modules/runner"))
    .dependsOn(core, http, httpDefault, feederJsonDefault)
    .settings(stdSettings("runner"))
    .settings(
      libraryDependencies := Dependencies.runner
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )

lazy val core =
  project
    .in(file("modules/core"))
    .settings(stdSettings("core"))
    .settings(
      libraryDependencies := Dependencies.core
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )

lazy val http =
  project
    .in(file("modules/http"))
    .dependsOn(core)
    .settings(stdSettings("http"))
    .settings(
      libraryDependencies := Dependencies.http
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )

lazy val httpDefault =
  project
    .in(file("modules/http-default"))
    .dependsOn(core, http)
    .settings(stdSettings("http-default"))
    .settings(
      libraryDependencies := Dependencies.httpDefault
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )

lazy val feederJsonDefault =
  project
    .in(file("modules/feeder-json-default"))
    .dependsOn(core)
    .settings(stdSettings("feeder-json-default"))
    .settings(
      libraryDependencies := Dependencies.feederJsonDefault
    )
    .settings(
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
    )
