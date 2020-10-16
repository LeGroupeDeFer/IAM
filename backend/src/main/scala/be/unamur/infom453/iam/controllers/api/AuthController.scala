package be.unamur.infom453.iam.controllers.api

import com.twitter.util.{Future => TwitterFuture}
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}
import be.unamur.infom453.iam.{lib => IAM}


object AuthController {

  case class RegisterRequest(username: String, password: String)

  case class LoginRequest(username: String, password: String)
  case class LoginResponse(token: String)

  case class LogoutRequest(username: String, token: String)
  case class User(id: Int, username: String)

  case class RefreshRequest(username: String, token: String)
  case class RefreshResponse(refresh: String, access: String)

  val routes: Router = Router.of[AuthController]

}

@Endpoint(path="/api/auth")
trait AuthController {

  import IAM._
  import AuthController._

  @Endpoint(method=HttpMethod.POST, path="/login")
  def login(r: LoginRequest): TwitterFuture[LoginResponse] =
    Auth.login(r.username, r.password)
      .map(LoginResponse)
      .recoverWith(ErrorResponse.recover[LoginResponse](403))

  @Endpoint(method=HttpMethod.POST, path="/logout")
  def logout(r: LogoutRequest): TwitterFuture[String] =
    Auth.logout(r.username, r.token)
      .map(_ => "Ok")
      .recoverWith(ErrorResponse.recover[String](403))

  @Endpoint(method=HttpMethod.POST, path="/refresh")
  def refresh(r: RefreshRequest): TwitterFuture[RefreshResponse] =
    Auth.refresh(r.username, r.token)
      .map(result => RefreshResponse(result._1, result._2))
      .recoverWith(ErrorResponse.recover[RefreshResponse](403))

  // TODO - Place account creation logic somewhere else
  @Endpoint(method=HttpMethod.POST, path="/register")
  def register(r: RegisterRequest): TwitterFuture[String] =
    Auth.register(r.username, r.password)
      .map(_ => "Ok")
      .recoverWith(ErrorResponse.recover[String](422))

}
