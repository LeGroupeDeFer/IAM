package be.unamur.infom453.iam.models

import java.sql.Timestamp

import be.unamur.infom453.iam.lib._

import scala.concurrent.{ExecutionContext, Future}


object CanTable {

  import api._
  import extensions._

  /* ------------------------ ORM class definition ------------------------ */

  case class Can(id: Option[Int], identifier: String, latitude: Double, longitude: Double, publicKey: String, signProtocol: String)

  class Cans(tag: Tag) extends Table[Can](tag, "cans") {

    // Columns
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def identifier: Rep[String] = column[String]("identifier")

    def latitude: Rep[Double] = column[Double]("latitude")

    def longitude: Rep[Double] = column[Double]("longitude")

    def publicKey: Rep[String] = column[String]("public_key", O.Length(2048))

    def signProtocol: Rep[String] = column[String]("sign_protocol")

    def deletedAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("deleted_at")

    // Projection
    def * = (id.?, identifier, latitude, longitude, publicKey, signProtocol) <> (Can.tupled, Can.unapply)

  }

  val cans = TableQuery[Cans]

  /* ---------------------    Queries Manipulation    --------------------- */

  /* --------------------- ORM Manipulation functions --------------------- */

  def all()(implicit ec: ExecutionContext, db: Database): Future[Seq[Can]] =
    queryAll(cans.active)

  /**
   * Parse all cans in the database. For each can, look for the associated data and return it as a whole.
   *
   * This function should have been implemented differently : use only one call to DB instead of two !
   * But you know, I chose the (bad and) easier way
   *
   * @return a sequence of tuples containing the can and its associated data
   */
  def allData()(implicit ec: ExecutionContext, db: Database): Future[Seq[(Can, Seq[CanData])]] = all()
    .flatMap(cansResult => Future.sequence(
      cansResult
        .map(_.identifier)
        .map(getWithData(_))
    ))

  def byIdentifier(identifier: String)(implicit ec: ExecutionContext, db: Database): Future[Can] =
    single(cans.active.withIdentifier(identifier))

  def getWithData(identifier: String)(implicit ec: ExecutionContext, db: Database): Future[(Can, Seq[CanData])] =
    queryAll(cans.active.withIdentifier(identifier).withData)
      .map(result => (result.head._1, result.flatMap(_._2)))

}
