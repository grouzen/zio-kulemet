package me.mnedokushev.zio.performance.testing.http.default
//
//import me.mnedokushev.zio.performance.testing.core.Action.ExecAction
//import me.mnedokushev.zio.performance.testing.core.Session
//import me.mnedokushev.zio.performance.testing.http.HttpAction
//import me.mnedokushev.zio.performance.testing.http.HttpAction.HttpResponse
//import me.mnedokushev.zio.performance.testing.http.default.DefaultHttpAction.DefaultHttpResponse
//import zio._
//
//case class DefaultHttpAction[S: Tag](override val headers: Map[String, String])
//    extends HttpAction[S, DefaultHttpResponse](headers) {
//
//  override def get(url: String): HttpAction[S, DefaultHttpResponse] =
//    DefaultHttpAction[S](headers) {
//      def run: RIO[S, DefaultHttpResponse] =
//        ZIO.serviceWithZIO[S](_ => ZIO.succeed(DefaultHttpResponse(200)))
//    }
//
//  override def header(name: String, value: String): HttpAction[S, DefaultHttpResponse] =
//    this.copy(headers.updated(name, value))
//
//}
//
//object DefaultHttpAction {
//
//  case class DefaultHttpResponse(status: Int) extends HttpResponse
//
//}
