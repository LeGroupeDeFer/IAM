package be.unamur.infom453.iam.controllers.api

import be.unamur.infom453.iam.models.{Can, CanData, CanTable}
import be.unamur.infom453.iam.lib._
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

import scala.concurrent.Future

object APIController {

  val routes: Router = Router.of[APIController]

  case class CanDataResponse(time: String, fillingRate: Double) {
    override def toString: String =
      "{\"time\":\"" + time + "\",\"fillingRate\":" + fillingRate + "}"
  }

  case class syncPayload(signature: String, data: CanDataResponse)

  object CanDataResponse {
    def from(canData: CanData): CanDataResponse =
      CanDataResponse(canData.moment.toString, canData.fillingRate)
  }


  case class CanResponse(
                          id: Int,
                          identifier: String,
                          latitude: Double,
                          longitude: Double,
                          data: Seq[CanDataResponse]
                        )

  object CanResponse {
    def from(can: Can, canData: Seq[CanData]): CanResponse =
      CanResponse(
        can.id.get,
        can.identifier,
        can.latitude,
        can.longitude,
        canData.map(CanDataResponse.from)
      )
  }

}

@Endpoint(path = "/api")
trait APIController {

  import APIController._

  @Endpoint(method = HttpMethod.GET, path = "/cans")
  def getAllCans: Future[Seq[Can]] = CanTable.all()

  @Endpoint(method = HttpMethod.GET, path = "/can/:identifier")
  def getOneCan(identifier: String): Future[CanResponse] = for {
    (a, b) <- CanTable.allData(identifier)
  } yield CanResponse.from(a, b)

  @Endpoint(method = HttpMethod.POST, path = "/can/:identifier/sync")
  def sync(identifier: String, payload: syncPayload): Future[String] = {
    (for {
      can <- CanTable.byIdentifier(identifier)
      publicKey = RSASign.publicKeyFromString(can.publicKey)
      _ <- RSASign.verify(publicKey, payload.signature, payload.data.toString)
    } yield "Ok").recoverWith(ErrorResponse.recover[String](406))
  }
}

