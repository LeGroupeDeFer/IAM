package be.unamur.infom453.iam.lib

import com.twitter.util.Future
import wvlet.airframe.http.{Http, HttpServerException, HttpStatus}

object ErrorResponse {
  def apply(code: Int, exception: Throwable): ErrorResponse =
    ErrorResponse(code, exception.getMessage)
}

case class ErrorResponse(code: Int, message: String) {

  def http: HttpServerException =
    Http.serverException(HttpStatus.ofCode(code)).withJsonOf(this)

  def future[A](): Future[A] =
    Future.exception(this.http)

}
