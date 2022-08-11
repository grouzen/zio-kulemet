package me.mnedokushev.zio.performance.testing.feeder.json.default

sealed trait FeederStrategy

object FeederStrategy {

  case object Sequential extends FeederStrategy
  case object Random     extends FeederStrategy
  case object Circular   extends FeederStrategy

}
