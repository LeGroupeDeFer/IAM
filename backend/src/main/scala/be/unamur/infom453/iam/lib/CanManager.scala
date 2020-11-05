package be.unamur.infom453.iam.lib

import be.unamur.infom453.iam.models._

import scala.concurrent.{ExecutionContext, Future}

object CanManager {

  import api._
  import extensions._

  def add(identifier: String, latitude: Double, longitude: Double, publicKey: String)
         (implicit ec: ExecutionContext, db: Database): Future[Can] = Future {
    if (identifier.isEmpty || publicKey.isEmpty) {
      throw missingAttribute
    }
  } flatMap { _ =>
    for {
      canId <- cans.insert(CanTable.Can(None, identifier, latitude, longitude, publicKey)).execute
    } yield CanTable.Can(Some(canId), identifier, latitude, longitude, publicKey)
  }

  def modify(identifier: String, latitude: Double, longitude: Double, publicKey: String)
            (implicit ec: ExecutionContext, db: Database): Future[Can] = Future {
    if (identifier.isEmpty || publicKey.isEmpty) {
      throw missingAttribute
    }
  }.flatMap { _ =>
    val c = new Can(None, identifier, latitude, longitude, publicKey)
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