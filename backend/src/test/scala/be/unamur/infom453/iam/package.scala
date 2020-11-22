package be.unamur.infom453

import org.scalatest.Tag
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api
import wvlet.airframe.http.{Http, HttpMessage, HttpSyncClient}
import wvlet.airframe.http.HttpMessage.{Request, Response}
import scala.concurrent.ExecutionContext.global

import scala.concurrent.ExecutionContext


package object iam {

  Configuration.parse(List())

  implicit val db: MySQLProfile.backend.DatabaseDef = {
    api.Database.forURL(
      "jdbc:mysql://database:3306/iam?useSSL=false",
      "iam",
      "secret",
      driver = "com.mysql.cj.jdbc.Driver"
    )
  }

  implicit val ec: ExecutionContext = global

  val client: HttpSyncClient[Request, Response] = Http
    .client
    .newSyncClient("http://localhost:8000")

  def isOk(response: HttpMessage.Response): Boolean =
    200 <= response.statusCode && response.statusCode <= 400

  object HttpTest extends Tag("be.unamur.infom453.iam.HttpTest")

}
