package me.mnedokushev.zio.performance.testing.http
//
//import me.mnedokushev.zio.performance.testing.core.Action.ExecAction
//import zio.Tag
//
//abstract class HttpAction[S: Tag, A](val headers: Map[String, String]) extends ExecAction[S, A] {
//
//  def get(url: String): HttpAction[S, A]
//
//  def header(name: String, value: String): HttpAction[S, A]
//
//}
//
//object HttpAction {
//
//  abstract class HttpGetAction[S: Tag, A] extends ExecAction[S, A]
//
//  trait HttpResponse {
//    val status: Int
//  }
//
//}
