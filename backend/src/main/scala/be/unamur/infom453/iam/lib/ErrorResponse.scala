package be.unamur.infom453.iam.lib

import com.twitter.util.Future
import wvlet.airframe.http.{Http, HttpServerException, HttpStatus}


object ErrorResponse {
  def apply(code: Int, exception: Throwable): ErrorResponse =
    ErrorResponse(code, exception.getMessage)

  val insertionError = new Exception("Unable to insert")
  val updateError = new Exception("Unable to update")
  val persistenceError = new Exception("This entity was not saved to the database yet")
  val invalidPassword = new Exception("Invalid password")
  val invalidAuth = new Exception("Invalid authentication IDs")
  val invalidToken = new Exception("Invalid token")
  val usernameTaken = new Exception("This username already used")

}

case class ErrorResponse(code: Int, message: String) {

  def http: HttpServerException =
    Http.serverException(HttpStatus.ofCode(code)).withJsonOf(this)

  def future[A](): Future[A] =
    Future.exception(this.http)

}
