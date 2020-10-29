package be.unamur.infom453.iam.lib

import be.unamur.infom453.iam.models._

import scala.concurrent.{ExecutionContext, Future}

object CanManager {

  import api._
  import extensions._

  def add(identifier: String, latitude: Double, longitude: Double, publicKey: String)(implicit ec: ExecutionContext, db: Database): Future[Can] = Future {
    if (identifier == "coucou") {
      throw new Exception("bite")
    }
  } flatMap { _ =>
    for {
      canId <- cans.insert(CanTable.Can(None, identifier, latitude, longitude, publicKey)).execute
    } yield CanTable.Can(Some(canId), identifier, latitude, longitude, publicKey)
  }

}
