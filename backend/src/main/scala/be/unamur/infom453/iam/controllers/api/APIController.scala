package be.unamur.infom453.iam.controllers.api

import be.unamur.infom453.iam.models.{Can, CanData, CanDataTable, CanTable}
import be.unamur.infom453.iam.lib._
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

import scala.concurrent.Future

object APIController {

  val routes: Router = Router.of[APIController]

  case class CanDataResponse(moment: String, fillingRate: Double)
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
  def cans: Future[Seq[Can]] = CanTable.all()

  @Endpoint(method = HttpMethod.GET, path = "/can/:identifier")
  def can(identifier: String): Future[CanResponse] = for {
    (a, b) <- CanTable.allData(identifier)
  } yield CanResponse.from(a, b)

}

