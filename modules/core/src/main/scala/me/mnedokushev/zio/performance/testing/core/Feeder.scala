package me.mnedokushev.zio.performance.testing.core

import me.mnedokushev.zio.performance.testing.core.FiniteFeeder.FeederStrategy
import zio.ZIO
import zio.stream._

trait Feeder[B] {

  def feed: ZStream[Any, String, B]

}

class FiniteFeeder[A](input: Option[List[A]], strategy: FeederStrategy = FeederStrategy.Sequential) extends Feeder[A] {

  import FeederStrategy._

  override def feed: ZStream[Any, String, A] =
    ZStream.unfold(input.getOrElse(List.empty))(pick)

  private def copy(strategy: FeederStrategy): FiniteFeeder[A] =
    new FiniteFeeder[A](input, strategy)

  def sequential: FiniteFeeder[A] =
    this.copy(strategy = Sequential)

  def circular: FiniteFeeder[A] =
    this.copy(strategy = Circular)

  def random: FiniteFeeder[A] =
    this.copy(strategy = Random)

  private def pick(data: List[A]): Option[(A, List[A])] =
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

object FiniteFeeder {

  sealed trait FeederStrategy

  object FeederStrategy {

    case object Sequential extends FeederStrategy
    case object Random     extends FeederStrategy
    case object Circular   extends FeederStrategy

  }

}

case class FeederZio[B](input: ZIO[Any, Nothing, B]) extends Feeder[B] {
  override def feed: ZStream[Any, String, B] =
    ZStream.fromZIO(input)
}
