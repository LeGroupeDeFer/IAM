package be.unamur.infom453.iam

import org.scalatest.FunSuite
import wvlet.airframe.http.{Http, HttpMessage}


class MainTest extends FunSuite {

  val client = Http.client.newSyncClient("http://localhost:8000")
  import client._

  def isOk(response: HttpMessage.Response): Boolean =
    200 <= response.statusCode && response.statusCode <= 400

  test("healthcheck") {
    val response = sendSafe(Http.request("/"))
    assert(isOk(response))
  }

  test("indexSendsClient") {
    val response = sendSafe(Http.request("/"))
    assert(response.header.get("Content-Type").contains("text/html"))
    assert(!response.message.toContentString.isEmpty)
  }

  test("staticEndpointsSendClient") {
    val admin = sendSafe(Http.request("/admin"))
    val auth = sendSafe(Http.request("/auth"))
    val responses = Seq(admin, auth)
    assert(responses.forall(isOk))
    assert(responses.forall(!_.message.toContentString.isEmpty))
  }

}