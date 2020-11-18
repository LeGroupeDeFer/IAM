package be.unamur.infom453.iam.controllers.api

import scala.concurrent.Future

import com.twitter.util.{Future => TwitterFuture}
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

import be.unamur.infom453.iam.lib._
import be.unamur.infom453.iam.lib.sign._
import be.unamur.infom453.iam.models.CanTable.Can


object AdminController extends Guide {

  /* ------------------------------- Routes -------------------------------- */

  val routes: Router = Router
    .add(Auth)
    .andThen[AdminController]

  /* --------------- Request / Response classes and objects ---------------- */

  sealed trait AdminRequest
  sealed trait AdminResponse

  case class NewCanRequest(
    id: String,
    longitude: Double,
    latitude: Double,
    publicKey: String,
    signProtocol: String
  ) {

    def isValid: Boolean = (id, signProtocol, publicKey) match {
      case ("", _, _)                                => false
      case (_, sp, _) if sign.from(sp).isEmpty       => false
      case (_, sp, "") if sp != NoneProtocol.code    => false
      case _                                         => true
    }

    def can: Can =
      if (!isValid) throw invalidAttribute
      else Can(
        None, this.id, this.longitude, this.latitude, this.publicKey,
        availableProtocols.find(_.code == this.signProtocol).get
      )

  }

  case class IdResponse(id: String)

}

/* ------------------------------ Controller ------------------------------- */

@Endpoint(path = "/api/admin")
trait AdminController {

  import AdminController._

  @Endpoint(method = HttpMethod.GET, path = "/noodles")
  def noodles = "Don't look now, but there is a multi-legged creature on your shoulder."

  @Endpoint(method = HttpMethod.POST, path = "/can")
  def createCan(canRequest: NewCanRequest): TwitterFuture[IdResponse] =
    canRequest
      .can
      .insert
      .map(c => IdResponse(c.identifier))
      .recoverWith(ErrorResponse.recover[IdResponse](422))

  @Endpoint(method = HttpMethod.PUT, path = "/can/:identifier")
  def updateCan(
    identifier: String,
    canRequest: NewCanRequest
  ): TwitterFuture[IdResponse] = ((identifier, canRequest) match {
    case (_, cr) if !cr.isValid     => Future.failed(missingAttribute)
    case (id, cr) if !(id == cr.id) => Future.failed(idMismatch)
    case (_, cr)                    => Future.successful(cr)
  }).flatMap((cr: NewCanRequest) => cr.can.update)
    .map(c => IdResponse(c.identifier))
    .recoverWith(ErrorResponse.recover[IdResponse](422))

  @Endpoint(method = HttpMethod.DELETE, path = "/can/:identifier")
  def removeCan(identifier: String): TwitterFuture[String] = Can
    .delete(identifier)
    .map(_ => "Ok")
    .recoverWith(ErrorResponse.recover[String](422))

}
