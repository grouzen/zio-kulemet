package me.mnedokushev.zio.performance.testing.feeder.json.default

import zio._
import zio.test._
import zio.json._

object FeederJsonDefaultSpec extends ZIOSpecDefault {

  case class Feed(foo: String, bar: Int)

  implicit val feedDecoder: JsonDecoder[Feed] = DeriveJsonDecoder.gen[Feed]

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FeederJsonDefaultSpec")(
      test("feed from string using sequential strategy successfully") {
        val input    =
          """[
            |  {
            |    "foo": "test",
            |    "bar": 1
            |  },
            |  {
            |    "foo": "test2",
            |    "bar": 2
            |  }
            |]
            |""".stripMargin
        val expected = Chunk(Feed("test", 1), Feed("test2", 2))

        val feeder = FeederJsonDefault[Feed](input).sequential

        for {
          result <- feeder.feed.runCollect
        } yield assertTrue(result == expected)
      },
      test("feed from string using circular strategy successfully") {
        val input    =
          """[
            |  {
            |    "foo": "test",
            |    "bar": 1
            |  },
            |  {
            |    "foo": "test2",
            |    "bar": 2
            |  }
            |]
            |""".stripMargin
        val expected = Chunk(
          Feed("test", 1),
          Feed("test2", 2),
          Feed("test", 1),
          Feed("test2", 2),
          Feed("test", 1)
        )

        val feeder = FeederJsonDefault[Feed](input).circular

        for {
          result <- feeder.feed.take(5).runCollect
        } yield assertTrue(result == expected)
      }
    )

}
