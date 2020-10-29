package be.unamur.infom453.iam.controllers.api

import com.twitter.util.{Future => TwitterFuture}
import be.unamur.infom453.iam.controllers.api.AdminAPI.{IdResponse, NewCanRequest}
import be.unamur.infom453.iam.lib._
import be.unamur.infom453.iam.models._
import com.twitter.util.Config.intoOption
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

object AdminAPI {

  case class NewCanRequest(id: String, longitude: Double, latitude: Double, publicKey: String)

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

  import be.unamur.infom453.iam.models.extensions._
  import be.unamur.infom453.iam.models._

  @Endpoint(method = HttpMethod.GET, path = "/noodles")
  def noodles = "Don't look now, but there is a multi-legged creature on your shoulder."

  @Endpoint(method = HttpMethod.POST, path = "/can")
  def createCan(newCan: NewCanRequest): TwitterFuture[IdResponse] =
    CanManager.add(newCan.id, newCan.latitude, newCan.latitude, newCan.publicKey)
      .map(p => IdResponse(p.identifier))
      .recoverWith {
        ErrorResponse.recover[IdResponse](422)
      }

}
