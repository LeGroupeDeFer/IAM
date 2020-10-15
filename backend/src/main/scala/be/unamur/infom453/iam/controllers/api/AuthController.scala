package be.unamur.infom453.iam.controllers.api

import com.twitter.util.Future
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}
import wvlet.airframe.http.HttpStatus
import be.unamur.infom453.iam.models._
import be.unamur.infom453.iam.Configuration._
import be.unamur.infom453.iam.Implicits._
import be.unamur.infom453.iam.lib.{Auth, ErrorResponse}
import wvlet.airframe.http.HttpStatus.{Forbidden_403, Unauthorized_401}

import scala.util.{Failure, Success}


object AuthController {

  case class LoginRequest(username: String, password: String)
  case class LogoutRequest(username: String)

  val routes: Router = Router.of[AuthController]

}

@Endpoint(path="/api/auth")
trait AuthController {

  import AuthController._

  @Endpoint(method=HttpMethod.POST, path="/login")
  def login(lr: LoginRequest): Future[User] =
    Auth.login(lr.username, lr.password).flatMap {
      case Success(token) => Future(token)
      case Failure(exception) => ErrorResponse(403, exception).future[User]
    }

  @Endpoint(method=HttpMethod.POST, path="/logout")
  def logout(lr: LogoutRequest): Future[User] =
    Auth.logout(lr.username).flatMap {
      case Success(token) => Future(token)
      case Failure(exception) => ErrorResponse(401, exception).future[User]
    }

  @Endpoint(method=HttpMethod.POST, path="/refresh")
  def refresh() = ???

}
