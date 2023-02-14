package me.mnedokushev.zio.performance.testing.core

import zio.ZState
import zio.stream.ZStream

trait Workload {
  val profile: ZStream[ZState[Session.State], String, Unit]
}
//
//case class OpenWorkload(profile: ZStream[ZState[Session.State], String, Unit]) extends Workload
//
//case class ClosedWorkload(profile: ZStream[ZState[Session.State], String, Unit]) extends Workload {
//
//  def constantConcurrentUsers(number: Int): ClosedWorkload
//
//}
