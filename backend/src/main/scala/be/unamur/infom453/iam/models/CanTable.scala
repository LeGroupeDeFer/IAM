package be.unamur.infom453.iam.models

import java.sql.Timestamp

import be.unamur.infom453.iam.lib._
import be.unamur.infom453.iam.models.CanDataTable._

import scala.concurrent.{ExecutionContext, Future}


object CanTable {

  import api._

  /* ------------------------ ORM class definition ------------------------ */

  case class Can(id: Option[Int], identifier: String, latitude: Double, longitude: Double, publicKey: String)

  class Cans(tag: Tag) extends Table[Can](tag, "cans") {

    // Columns
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def identifier: Rep[String] = column[String]("identifier")

    def latitude: Rep[Double] = column[Double]("latitude")

    def longitude: Rep[Double] = column[Double]("longitude")

    def publicKey: Rep[String] = column[String]("public_key", O.Length(2048))

    def deleted_at: Rep[Option[Timestamp]] = column[Option[Timestamp]]("deleted_at")

    // Projection
    def * = (id.?, identifier, latitude, longitude, publicKey) <> (Can.tupled, Can.unapply)

  }

  val cans = TableQuery[Cans]

  /* ---------------------    Queries Manipulation    --------------------- */

  implicit class CanExtension[C[_]](q: Query[CanTable.Cans, CanTable.Can, C]) {
    // specify mapping of relationship to address
    def active =
      q.filter(_.deleted_at.isEmpty)

    def withIdentifier(identifier: String): Query[Cans, Can, C] =
      q.filter(_.identifier === identifier)

    def withId(id: Int): Query[Cans, Can, C] =
      q.filter(_.id === id)

  }

  /* --------------------- ORM Manipulation functions --------------------- */

  def all()(implicit ec: ExecutionContext, db: Database): Future[Seq[Can]] =
    queryAll(cans.active)

  def byIdentifier(identifier: String)(implicit ec: ExecutionContext, db: Database): Future[Can] =
    single(cans.active.withIdentifier(identifier))


  def allData(identifier: String)(implicit ec: ExecutionContext, db: Database): Future[(Can, Seq[CanData])] = {
    val tab = queryAll(cans.active.withIdentifier(identifier).withData)
    tab.map(t => {
      val h = t.head._1
      val v = t.map(_._2)
      (h, v.flatten)
    })
  }


}
