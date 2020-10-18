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


  case class CansResponse(id: Int, identifier: String, latitude: Double, longitude: Double, data: Seq[CanDataResponse])
  object CansResponse {
    def from(can: Can, canData: Seq[CanData]): CansResponse =
      CansResponse(can.id.get, can.identifier, can.latitude, can.longitude, canData.map(CanDataResponse.from))
  }

}

@Endpoint(path = "/api")
trait APIController {

  import APIController._

  @Endpoint(method = HttpMethod.GET, path = "/noodles")
  def noodles = "You single-handedly fought your way into this hopeless mess."

  @Endpoint(method = HttpMethod.GET, path = "/cans")
  def cans: Future[Seq[CanTable.Can]] = CanTable.all()

  @Endpoint(method = HttpMethod.GET, path = "/can/:identifier")
  def can(identifier: String): Future[CansResponse] = for {
    (a, b) <- CanTable.allData(identifier)
  } yield CansResponse.from(a, b)

}

