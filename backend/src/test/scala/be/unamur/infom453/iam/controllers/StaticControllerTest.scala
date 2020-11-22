package be.unamur.infom453.iam.controllers

import be.unamur.infom453.iam._
import org.scalatest._
import org.scalatest.flatspec._
import wvlet.airframe.http.Http


class StaticControllerTest extends AnyFlatSpec {

  import client._

  "The index" should "respond" taggedAs HttpTest in {
    val response = sendSafe(Http.request("/"))
    assert(isOk(response))
  }

  "The index" should "send the client" taggedAs HttpTest in {
    val response = sendSafe(Http.request("/"))
    assert(response.header.get("Content-Type").contains("text/html"))
    assert(!response.message.toContentString.isEmpty)
  }

  "The static pages" should "send the client" taggedAs HttpTest in {
    val admin = sendSafe(Http.request("/admin"))
    val auth = sendSafe(Http.request("/auth"))
    val responses = Seq(admin, auth)
    assert(responses.forall(isOk))
    assert(responses.forall(!_.message.toContentString.isEmpty))
  }

}