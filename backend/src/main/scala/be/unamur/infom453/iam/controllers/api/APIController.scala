package be.unamur.infom453.iam.controllers.api


import be.unamur.infom453.iam.lib._
import be.unamur.infom453.iam.models.CanSampleTable.CanSample
import be.unamur.infom453.iam.models.CanTable.Can
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

import scala.concurrent.Future
import scala.util.{Success, Failure, Try}

object APIController extends Guide {

  /* ------------------------------- Routes -------------------------------- */

  val routes: Router = Router.of[APIController]

  /* --------------- Request / Response classes and objects ---------------- */

  sealed trait APIRequest
  sealed trait APIResponse

  object CanSampleResponse {

    def from(canSample: CanSample): CanSampleResponse =
      CanSampleResponse(canSample.moment.toString, canSample.fillingRate)

  }

  case class CanSampleResponse(
    time: String,
    fillingRate: Double
  ) extends APIResponse {

    override def toString: String =
      s"""{"time":"${time}","fillingRate":${fillingRate}}"""

  }

  object CanResponse {

    def from(can: Can, canSample: Seq[CanSample]): CanResponse = {
      val currentFill: Double = Try(canSample.maxBy(_.moment).fillingRate) match {
          case Success(value) => value
          case Failure(value) => 0.0
        }
      CanResponse(
        can.identifier,
        can.longitude,
        can.latitude,
        can.publicKey,
        can.signProtocol.code,
        currentFill,
        canSample.map(CanSampleResponse.from)
      )
    }
  }

  type CanSampleRequest = CanSampleResponse

  case class CanResponse(
    id: String,
    longitude: Double,
    latitude: Double,
    publicKey: String,
    signProtocol: String,
    currentFill: Double,
    data: Seq[CanSampleResponse]
  ) extends APIResponse

  case class SyncRequest(
    signature: String,
    data: CanSampleRequest
  ) extends APIRequest

}

/* ------------------------------ Controller ------------------------------- */

@Endpoint(path = "/api")
trait APIController {

  import APIController._

  @Endpoint(method = HttpMethod.GET, path = "/cans")
  def getAllCans: Future[Seq[CanResponse]] = Can
    .allSampled
    .map(_.map(row => CanResponse.from(row._1, row._2)))

  @Endpoint(method = HttpMethod.GET, path = "/can/:identifier")
  def getOneCan(identifier: String): Future[CanResponse] = Can
    .byIdentifierSampled(identifier)
    .map(result => CanResponse.from(result._1, result._2))
    .recoverWith(ErrorResponse.recover[CanResponse](404))

  @Endpoint(method = HttpMethod.POST, path = "/can/:identifier/sync")
  def sync(identifier: String, payload: SyncRequest): Future[String] = Can
    .byIdentifier(identifier)
    .flatMap(can =>
      can.signProtocol.verify(
        can.publicKey,
        payload.signature,
        payload.data.toString
      ).map(_ => can)
    ).flatMap(can =>
      can.add(CanSample(
        None,
        can.id.get,
        instantFromString(payload.data.time),
        payload.data.fillingRate
      )).map(_ => "Ok")
    ).recoverWith(ErrorResponse.recover[String](406))

}

