package be.unamur.infom453.iam.lib

import scala.concurrent.Future
import wvlet.airframe.http.{Http, HttpServerException, HttpStatus}


object ErrorResponse {

  def apply(code: Int, exception: Throwable): ErrorResponse =
    ErrorResponse(code, exception.getMessage)

  def recover[A](code: Int): PartialFunction[Throwable, Future[A]] =
    new PartialFunction[Throwable, Future[A]] {
      def apply(e: Throwable) = ErrorResponse(code, e).future[A]
      override def isDefinedAt(x: Throwable): Boolean = x.isInstanceOf[Exception]
    }

}

case class ErrorResponse(code: Int, message: String) {

  def http: HttpServerException =
    Http.serverException(HttpStatus.ofCode(code)).withJsonOf(this)

  def future[A]: Future[A] =
    Future.failed(http)

}
