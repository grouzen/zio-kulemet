package me.mnedokushev.zio.performance.testing.feeder.json.default

import me.mnedokushev.zio.performance.testing.core.Feeder
import me.mnedokushev.zio.performance.testing.feeder.json.default.FeederStrategy._
import zio.stream.ZStream
import zio.json._
import zio.nio.file.Path

import java.net.URI

case class FeederJsonDefault[R, B: JsonDecoder](input: String, strategy: FeederStrategy = FeederStrategy.Sequential)
    extends Feeder[String, R, B] {

  override def feed: ZStream[R, String, B] = {
    val parsingResult = input.fromJson[List[B]]

    parsingResult match {
      case Left(reason) =>
        ZStream.fail(reason)
      case Right(data)  =>
        ZStream.unfold(Option(data))(_.flatMap(pick))
    }
  }

  def sequential: FeederJsonDefault[R, B] = this.copy(strategy = Sequential)

  def circular: FeederJsonDefault[R, B] = this.copy(strategy = Circular)

  def random: FeederJsonDefault[R, B] = this.copy(strategy = Random)

  private def pick(data: List[B]): Option[(B, Option[List[B]])] =
    strategy match {
      case Sequential =>
        data match {
          case head :: tail => Some(head -> Some(tail))
          case Nil          => None
        }
      case Circular   =>
        data match {
          case head :: tail => Some(head -> Some(tail :+ head))
          case Nil          => None
        }
      case Random     =>
        // TODO: not efficient
        data
          .lift(scala.util.Random.nextInt(data.length))
          .map(_ -> Some(data))

    }

}

object FeederJsonDefault {

  def apply[R, E, B: JsonDecoder](file: Path): FeederJsonDefault[R, B] = ???

  def apply[R, E, B: JsonDecoder](uri: URI): FeederJsonDefault[R, B] = ???

}
