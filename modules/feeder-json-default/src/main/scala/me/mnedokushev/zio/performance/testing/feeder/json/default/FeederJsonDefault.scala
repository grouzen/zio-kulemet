package me.mnedokushev.zio.performance.testing.feeder.json.default

import me.mnedokushev.zio.performance.testing.core.FiniteFeeder
import me.mnedokushev.zio.performance.testing.core.FiniteFeeder.FeederStrategy
import zio.json._
import zio.nio.file.Path

import java.net.URI

case class FeederJsonDefault[B: JsonDecoder](input0: String, strategy: FeederStrategy = FeederStrategy.Sequential)
    extends FiniteFeeder[B](input0.fromJson[List[B]].toOption)

object FeederJsonDefault {

  def apply[R, E, B: JsonDecoder](file: Path): FeederJsonDefault[B] = ???

  def apply[R, E, B: JsonDecoder](uri: URI): FeederJsonDefault[B] = ???

}
