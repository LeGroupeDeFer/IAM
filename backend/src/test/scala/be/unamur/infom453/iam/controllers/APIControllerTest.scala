package be.unamur.infom453.iam.controllers

import be.unamur.infom453.iam._
import be.unamur.infom453.iam.controllers.api.APIController.CanResponse
import be.unamur.infom453.iam.lib.Auth
import be.unamur.infom453.iam.lib.sign.NoneProtocol
import be.unamur.infom453.iam.models.Ops._
import be.unamur.infom453.iam.models.users
import be.unamur.infom453.iam.models.CanTable.{cans, Can}
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers.must.Matchers
import wvlet.airframe.http.Http

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


class APIControllerTest extends AnyFlatSpec
  with BeforeAndAfterAll
  with Matchers {

  import client._
  import slick.jdbc.MySQLProfile.api._

  override def beforeAll: Unit = {
    Configuration.parse(List())
    Await.result(Future.sequence(Seq(
      Auth.register("obi-wan", "secret"),
      cans.delete.execute.flatMap(_ => Future.sequence(Seq(
        Can(None, "a", 25.17, 36.49, "a", NoneProtocol).insert,
        Can(None, "b", 25.17, 36.49, "b", NoneProtocol).insert,
        Can(None, "c", 25.17, 36.49, "c", NoneProtocol).insert,
        Can(None, "d", 25.17, 36.49, "d", NoneProtocol).insert,
      )))
    )), Duration.Inf)
  }

  "/api/cans" should "give me all the cans" in {
    val response = sendSafe(Http.GET("/api/cans"))

    response.statusCode must equal(200)
    assert(response.isContentTypeJson)

    val cansResponse = decode[Seq[CanResponse]](response.message.toContentString)
    assert(cansResponse.isRight)

    val identifiers = cansResponse.getOrElse(Seq(): Seq[CanResponse]).map(_.id)
    identifiers must contain theSameElementsAs Seq("a", "b", "c", "d")
  }

  override def afterAll: Unit = {
    Await.ready(Future.sequence(Seq(
      users.withUsername("obi-wan").delete.execute,
      cans.withIdentifier("a").delete.execute,
      cans.withIdentifier("b").delete.execute,
      cans.withIdentifier("c").delete.execute,
      cans.withIdentifier("d").delete.execute,
    )), Duration.Inf)
  }

}
