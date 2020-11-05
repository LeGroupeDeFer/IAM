package be.unamur.infom453.iam.lib

import be.unamur.infom453.iam.models.CanDataTable.CanData
import be.unamur.infom453.iam.models._

import scala.concurrent.{ExecutionContext, Future}

object CanManager {

  import api._
  import extensions._

  def add(identifier: String, latitude: Double, longitude: Double, publicKey: String, signProtocol: String)
         (implicit ec: ExecutionContext, db: Database): Future[Can] = Future {
    if (identifier.isEmpty) {
      throw missingAttribute
    }
  } flatMap { _ =>
    for {
      canId <- cans.insert(CanTable.Can(None, identifier, latitude, longitude, publicKey, signProtocol)).execute
    } yield CanTable.Can(Some(canId), identifier, latitude, longitude, publicKey, signProtocol)
  }

  def addData(identifier: String, time: String, fillingRate: Double)
             (implicit ec: ExecutionContext, db: Database): Future[Boolean] =
    CanTable.byIdentifier(identifier).map(can => {
      CanDataTable.canDatas.insert(CanData(None, can.id.get, instantFromString(time), fillingRate)).execute
    }).map(_ => true)

  def modify(identifier: String, latitude: Double, longitude: Double, publicKey: String, signProtocol: String)
            (implicit ec: ExecutionContext, db: Database): Future[Can] = Future {
    if (identifier.isEmpty || publicKey.isEmpty) {
      throw missingAttribute
    }
  }.flatMap { _ =>
    val c = new Can(None, identifier, latitude, longitude, publicKey, signProtocol)
    for {
      modified <- cans
        .withIdentifier(identifier)
        .update(c)
        .execute
    } yield if (modified != 1) throw updateError else c
  }.flatMap { _ =>
    for {
      yesWeCan <- cans.withIdentifier(identifier).one
    } yield yesWeCan
  }

  def remove(identifier: String)(implicit ec: ExecutionContext, db: Database): Future[Boolean] =
    for {
      modified <- cans.withIdentifier(identifier).delete.execute
    } yield if (modified != 1) throw updateError else true
}