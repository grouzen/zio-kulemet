import sbt._

object Dependencies {

  object version {
    val zio     = "2.0.0"
    val zioHttp = "2.0.0-RC10"
  }

  object org {
    val zio = "dev.zio"
    val d11 = "io.d11"
  }

  lazy val zio        = org.zio %% "zio"          % version.zio
  lazy val zioNio     = org.zio %% "zio-nio"      % version.zio
  lazy val zioMacros  = org.zio %% "zio-macros"   % version.zio
  lazy val zioPrelude = org.zio %% "zio-prelude"  % "1.0.0-RC15" // supports zio2 since 1.0.0-RC15
  lazy val zioJson    = org.zio %% "zio-json"     % "0.3.0-RC10"
  lazy val zioTest    = org.zio %% "zio-test"     % version.zio
  lazy val zioTestSbt = org.zio %% "zio-test-sbt" % version.zio

  lazy val zioHttp = org.d11 %% "zhttp" % version.zioHttp

  lazy val runner = Seq(
    zio,
    zioNio,
    zioPrelude,
    zioMacros,
    zioTest    % Test,
    zioTestSbt % Test
  )

  lazy val core = Seq(
    zio,
    zioNio,
    zioPrelude,
    zioMacros,
    zioTest    % Test,
    zioTestSbt % Test
  )

  lazy val http = Seq(
    zio,
    zioNio,
    zioPrelude,
    zioMacros,
    zioTest    % Test,
    zioTestSbt % Test
  )

  lazy val httpDefault = Seq(
    zio,
    zioPrelude,
    zioMacros,
    zioHttp,
    zioTest    % Test,
    zioTestSbt % Test
  )

  lazy val feederJsonDefault = Seq(
    zio,
    zioPrelude,
    zioMacros,
    zioJson,
    zioTest    % Test,
    zioTestSbt % Test
  )

}
