package be.unamur.infom453.iam.controllers.api

import be.unamur.infom453.iam.models.{CanDataTable, CanTable}
import be.unamur.infom453.iam.lib._
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

import scala.concurrent.Future

object APIController {

  val routes: Router = Router.of[APIController]

  case class CanDataControllerObject(moment: String, filling_rate: Double)

  def fromCanData(o: CanDataTable.CanData): CanDataControllerObject =
    CanDataControllerObject(o.moment.toString, o.filling_rate)

}

@Endpoint(path = "/api")
trait APIController {

  import APIController._

  @Endpoint(method = HttpMethod.GET, path = "/noodles")
  def noodles = "You single-handedly fought your way into this hopeless mess."

  @Endpoint(method = HttpMethod.GET, path = "/cans")
  def getCans: Future[Seq[CanTable.Can]] = CanTable.all()

  @Endpoint(method = HttpMethod.GET, path = "/can/:identifier")
  def getCanData(identifier: String): Future[(CanTable.Can, Seq[CanDataControllerObject])] = {
    val info = CanTable.allData(identifier)
    for {
      (a, b) <- info
      tab = b.map(fromCanData)
    } yield (a, tab)

  }
}

