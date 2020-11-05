package be.unamur.infom453.iam.controllers.api

import com.twitter.util.{Future => TwitterFuture}
import be.unamur.infom453.iam.controllers.api.AdminAPI.{IdResponse, NewCanRequest}
import be.unamur.infom453.iam.lib._
import be.unamur.infom453.iam.lib.sign._
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

object AdminAPI {

  case class NewCanRequest(id: String, longitude: Double, latitude: Double, publicKey: String, signProtocol: String) {
    def validate(): Boolean = {
      if (id == "" || !sign.availableProtocols.map(_.code).contains(signProtocol)) {
        return false
      }
      if (signProtocol != NoneProtocol.code && publicKey.isEmpty) {
        return false
      }

      true
    }
  }

  case class IdResponse(id: String)

}

object AdminController {

  val routes = Router
    .add(Auth)
    .andThen[AdminController]

}

// These endpoints are behind the auth
@Endpoint(path = "/api/admin")
trait AdminController {

  import be.unamur.infom453.iam.models._

  @Endpoint(method = HttpMethod.GET, path = "/noodles")
  def noodles = "Don't look now, but there is a multi-legged creature on your shoulder."

  @Endpoint(method = HttpMethod.POST, path = "/can")
  def createCan(newCan: NewCanRequest): TwitterFuture[IdResponse] = TwitterFuture {
    if (!newCan.validate()) {
      throw missingAttribute
    }
    newCan
  }.flatMap(c => CanManager.add(c.id, c.latitude, c.longitude, c.publicKey, c.signProtocol))

    .map(p => IdResponse(p.identifier))
    .recoverWith(ErrorResponse.recover[IdResponse](422))

  @Endpoint(method = HttpMethod.PUT, path = "/can/:identifier")
  def updateCan(identifier: String, newCan: NewCanRequest): TwitterFuture[IdResponse] = TwitterFuture {
    if (!newCan.validate()) {
      throw missingAttribute
    }
    if (!identifier.equals(newCan.id)) {
      throw idMismatch
    }
    newCan
  }.flatMap(c => CanManager.modify(c.id, c.latitude, c.longitude, c.publicKey, c.signProtocol))
    .map(c => IdResponse(c.identifier))
    .recoverWith(ErrorResponse.recover[IdResponse](422))

  @Endpoint(method = HttpMethod.DELETE, path = "/can/:identifier")
  def removeCan(identifier: String): TwitterFuture[String] =
    CanManager.remove(identifier).map(_ => "Ok").recoverWith(ErrorResponse.recover[String](422))
}