package me.mnedokushev.zio.performance.testing.feeder.json.default

import me.mnedokushev.zio.performance.testing.core.Feeder
import me.mnedokushev.zio.performance.testing.feeder.json.default.FeederStrategy._
import zio.stream.ZStream
import zio.json._
import zio.nio.file.Path

import java.net.URI

case class FeederJsonDefault[B: JsonDecoder](input: String, strategy: FeederStrategy = FeederStrategy.Sequential)
    extends Feeder[String, B] {

  override def feed: ZStream[Any, String, B] = {
    val parsingResult = input.fromJson[List[B]]

    parsingResult match {
      case Left(reason) =>
        ZStream.fail(reason)
      case Right(data)  =>
        ZStream.unfold(data)(pick)
    }
  }

  def sequential: FeederJsonDefault[B] =
    this.copy(strategy = Sequential)

  def circular: FeederJsonDefault[B] =
    this.copy(strategy = Circular)

  def random: FeederJsonDefault[B] =
    this.copy(strategy = Random)

  private def pick(data: List[B]): Option[(B, List[B])] =
    strategy match {
      case Sequential =>
        data match {
          case head :: tail => Some(head -> tail)
          case Nil          => None
        }
      case Circular   =>
        data match {
          case head :: tail => Some(head -> (tail :+ head))
          case Nil          => None
        }
      case Random     =>
        // TODO: not efficient
        data
          .lift(scala.util.Random.nextInt(data.length))
          .map(_ -> data)

    }

}

object FeederJsonDefault {

  def apply[R, E, B: JsonDecoder](file: Path): FeederJsonDefault[B] = ???

  def apply[R, E, B: JsonDecoder](uri: URI): FeederJsonDefault[B] = ???

}
