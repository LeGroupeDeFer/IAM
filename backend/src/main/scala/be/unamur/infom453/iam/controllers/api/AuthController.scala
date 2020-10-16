package be.unamur.infom453.iam.controllers.api

import com.twitter.util.Future
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}
import be.unamur.infom453.iam.Implicits._
import be.unamur.infom453.iam.lib.{Auth, ErrorResponse}
import be.unamur.infom453.iam.models._


object AuthController {

  case class RegisterRequest(username: String, password: String)

  case class LoginRequest(username: String, password: String)
  case class LoginResponse(token: String)

  case class LogoutRequest(username: String, token: String)
  case class User(id: Int, username: String)

  case class RefreshRequest(username: String, token: String)
  case class RefreshResponse(token: String)

  val routes: Router = Router.of[AuthController]

}

@Endpoint(path="/api/auth")
trait AuthController {

  import AuthController._
  import be.unamur.infom453.iam.Implicits._

  @Endpoint(method=HttpMethod.POST, path="/login")
  def login(lr: LoginRequest): Future[LoginResponse] =
    Auth.login(lr.username, lr.password)
      .map(LoginResponse)
      .recoverWith{ case e: Exception => ErrorResponse(403, e).future[LoginResponse] }

  @Endpoint(method=HttpMethod.POST, path="/logout")
  def logout(lr: LogoutRequest): Future[String] =
    Auth.logout(lr.username, lr.token)
      .map(_ => "Ok")
      .recoverWith { case e: Exception => ErrorResponse(403, e).future[String] }

  @Endpoint(method=HttpMethod.POST, path="/register")
  def register(rr: RegisterRequest): Future[String] =
    Auth.register(rr.username, rr.password)
      .map(_ => "Ok")
      .recoverWith { case e: Exception => ErrorResponse(422, e).future[String] }

  @Endpoint(method=HttpMethod.POST, path="/refresh")
  def refresh(rr: RefreshRequest): Future[RefreshResponse] =
    Auth.refresh(rr.username, rr.token)
      .map(RefreshResponse)
      .recoverWith { case e: Exception => ErrorResponse(403, e).future[RefreshResponse] }

}
