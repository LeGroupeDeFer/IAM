package be.unamur.infom453.iam.models

import java.sql.Timestamp

import scala.concurrent.{ExecutionContext, Future}
import be.unamur.infom453.iam.lib._
import be.unamur.infom453.iam.lib.sign.{availableProtocols, SignProtocol}


object CanTable {

  import api._
  import Ops._

  /* ------------------------ ORM class definition ------------------------- */

  object Can {

    def all()(implicit ec: ExecutionContext, db: Database): Future[Seq[Can]] =
      cans.active.execute

    def byIdentifier(identifier: String)(
      implicit ec: ExecutionContext,
      db         : Database
    ): Future[Can] = cans
      .active
      .withIdentifier(identifier)
      .execute
      .map(_.head)

    def byIdentifierSampled(
      identifier: String
    ): Future[(Can, Seq[CanSample])] = cans
      .active
      .withIdentifier(identifier)
      .withData
      .execute
      .map {
        case Nil => throw unknownIdentifier
        case xs => (xs.head._1, xs.flatMap(_._2))
      }

    def delete(identifier: String): Future[Unit] = cans
      .withIdentifier(identifier)
      .remove
      .execute
      .map(modified => if (modified != 1) throw updateError else ())

    /**
     * Parse all cans in the database. For each can, fetch the associated
     * samples.
     *
     * @return a sequence of tuples containing the can and its associated
     *         samples
     */
    def allSampled: Future[Seq[(Can, Seq[CanSample])]] = cans
      .withData
      .execute
      .map(row => {
        row.groupBy(_._1.id.get).map { case (_, tuples) =>
          val canOption = tuples.headOption.map(_._1)
          canOption.map(
            (_, tuples.map(_._2).filter(_.isDefined).map(_.get))
          )
        }.toSeq
      }.filter(_.isDefined).map(_.get))

  }

  case class Can(
    id: Option[Int],
    identifier: String,
    latitude: Double,
    longitude: Double,
    publicKey: String,
    signProtocol: SignProtocol
  ) {

    def inserted: Boolean = this.id.isDefined

    def insert(implicit ec: ExecutionContext, db : Database): Future[Can] =
      (this.id, this.identifier) match {
        case (Some(_), _) => Future.failed(alreadyInserted)
        case (_, "")      => Future.failed(missingAttribute)
        case _            => cans
          .insert(this)
          .execute
          .map(id => this.copy(id=Some(id)))
      }

    def update(
      implicit ec: ExecutionContext,
      db         : Database
    ): Future[Can] =
      (this.identifier, this.publicKey) match {
        case ("", _) | (_, "") => Future.failed(missingAttribute)
        case _                 => cans
          .withIdentifier(this.identifier)
          .update(this)
          .execute
          .map(m => if (m != 1) throw updateError else m)
          .map(_ => this)
      }

    def delete(
      implicit ec: ExecutionContext,
      db         : Database
    ): Future[Unit] = Can.delete(this.identifier)

    def add(sample: CanSample)(
      implicit ec: ExecutionContext,
      db         : Database
    ): Future[CanSample] = canSamples
      .insert(sample.copy(canId=this.id.get))
      .execute
      .map(id => sample.copy(id = Some(id), canId=this.id.get))

    override def equals(obj: Any): Boolean = obj match {
      case that: Can => that.id == this.id
      case _         => false
    }

  }

  /* ----------------------------- Projection ------------------------------ */

  type CanTuple = (Option[Int], String, Double, Double, String, String)

  // Tuple -> Can
  private def canApply(t: CanTuple): Can = try {
    val signProtocol = availableProtocols.find(_.code == t._6).get
    Can(t._1, t._2, t._3, t._4, t._5, signProtocol)
  } catch { case _: NoSuchElementException => throw invalidAttribute }

  // Can -> Tuple
  private def canUnapply(c: Can): Option[CanTuple] = Some(
    c.id, c.identifier, c.latitude, c.longitude, c.publicKey,
    c.signProtocol.code
  )

  /* -------------------------- Table definition --------------------------- */

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
    def * = (
      id.?, identifier, latitude, longitude, publicKey, signProtocol
    ) <> (canApply, canUnapply)

  }

  val cans = TableQuery[Cans]

}
