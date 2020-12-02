package be.unamur.infom453.iam.controllers

import be.unamur.infom453.iam._
import be.unamur.infom453.iam.controllers.api.AuthController._
import be.unamur.infom453.iam.lib.Auth
import be.unamur.infom453.iam.models.Ops._
import be.unamur.infom453.iam.models.users
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers.must.Matchers
import wvlet.airframe.http.Http

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class AuthControllerTest extends AnyFlatSpec
  with BeforeAndAfterAll
  with Matchers {

  import client._
  import slick.jdbc.MySQLProfile.api._

  var accessToken: String   = ""
  var refreshToken: String  = ""


  override def beforeAll: Unit = {
    Configuration.parse(List())
    Await.result(
      Auth.register("batman", "secret"),
      Duration.Inf
    )
  }


  "/api/auth/login" should "not log me in" in {
    val response = sendSafe(Http
      .POST("/api/auth/login")
      .withJsonOf(LoginRequest("batman", "notTheSecret"))
    )

    response.statusCode must equal(403)
  }


  "/api/auth/login" should "log me in" in {
    val response = sendSafe(Http
      .POST("/api/auth/login")
      .withJsonOf(LoginRequest("batman", "secret"))
    )

    response.statusCode must equal(200)
    response.isContentTypeJson must be(true)

    val loginResponse = decode[LoginResponse](response.message.toContentString)
    loginResponse.isRight must be(true)

    loginResponse.foreach(lr => refreshToken = lr.token)
  }


  "/api/auth/refresh" should "not refresh" in {
    val response = sendSafe(Http
      .POST("/api/auth/refresh")
      .withJsonOf(RefreshRequest("batman", "imNotAToken"))
    )

    response.statusCode must equal(403)
  }


  "/api/auth/refresh" should "refresh" in {
    val response = sendSafe(Http
      .POST("/api/auth/refresh")
      .withJsonOf(RefreshRequest("batman", refreshToken))
    )

    response.statusCode must equal(200)
    response.isContentTypeJson must be(true)

    val refreshResponse = decode[RefreshResponse](response.message.toContentString)
    refreshResponse.isRight must be(true)

    refreshResponse.foreach(rr => {
      refreshToken = rr.refresh
      accessToken = rr.access
    })
  }


  override def afterAll: Unit = {
    Await.ready(
      users.withUsername("batman").delete.execute,
      Duration.Inf
    )
  }

}
