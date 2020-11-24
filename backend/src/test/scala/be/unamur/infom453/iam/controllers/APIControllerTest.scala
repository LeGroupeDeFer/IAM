package be.unamur.infom453.iam.controllers

import java.security.{KeyFactory, KeyPair, Signature}
import java.time.Instant

import be.unamur.infom453.iam._
import be.unamur.infom453.iam.controllers.api.APIController.{CanResponse, CanSampleResponse, SyncRequest}
import be.unamur.infom453.iam.lib.Auth
import be.unamur.infom453.iam.lib.stringFromInstant
import be.unamur.infom453.iam.lib.sign.{ED25519Protocol, NoneProtocol, RSAProtocol, SignProtocol}
import be.unamur.infom453.iam.models.Ops._
import be.unamur.infom453.iam.models.users
import be.unamur.infom453.iam.models.CanTable.{cans, Can}
import io.circe.generic.auto._
import io.circe.parser.decode
import org.scalatest._
import org.scalatest.flatspec._
import org.scalatest.matchers.must.Matchers
import wvlet.airframe.http.Http
import org.apache.commons.codec.binary.Base64

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


class APIControllerTest extends AnyFlatSpec
  with BeforeAndAfterAll
  with Matchers {

  import client._
  import slick.jdbc.MySQLProfile.api._

  private val rsaSignature    = RSAProtocol.signature
  private val rsaKeyFactory   = RSAProtocol.keyFactory
  private val rsaGenerator    = RSAProtocol.generator
  private val eddsaSignature  = ED25519Protocol.signature
  private val eddsaKeyFactory = ED25519Protocol.keyFactory
  private val eddsaGenerator  = ED25519Protocol.generator


  override def beforeAll: Unit = {
    Configuration.parse(List())

    val timestamp         = stringFromInstant(Instant.now())
    val canSampleRequest  = CanSampleResponse(timestamp, 71.14)

    // RSA
    val rsaPair           = rsaGenerator.generateKeyPair()
    val rsaPublicKey      = rsaPair.getPublic
    val rsaPrivateKey     = rsaPair.getPrivate
    val rsaCan            = Can(None, "rsaCan", 42.42, 42.42, rsaPublicKey.toString, RSAProtocol)

    rsaSignature.initSign(rsaPrivateKey)
    rsaSignature.update(canSampleRequest.toString.getBytes("UTF-8"))
    val b64RsaSignedRequest = Base64.encodeBase64(rsaSignature.sign())
    val rsaPayload = SyncRequest(b64RsaSignedRequest.toString, canSampleRequest)

    // ED25519
    // val eddsaPair = eddsaGenerator.generateKeyPair()

    Await.result(Future.sequence(Seq(
      Auth.register("obi-wan", "secret"),
      cans.delete.execute.flatMap(_ => Future.sequence(Seq(
        Can(None, "a", 25.17, 36.49, "a", NoneProtocol).insert,
        Can(None, "b", 25.17, 36.49, "b", NoneProtocol).insert,
        Can(None, "c", 25.17, 36.49, "c", NoneProtocol).insert,
        Can(None, "d", 25.17, 36.49, "d", NoneProtocol).insert,
        rsaCan.insert
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
    identifiers must contain theSameElementsAs Seq("a", "b", "c", "d", "rsaCan")
  }

  override def afterAll: Unit = {
    Await.ready(Future.sequence(Seq(
      users.withUsername("obi-wan").delete.execute,
      cans.delete.execute,
    )), Duration.Inf)
  }

}
