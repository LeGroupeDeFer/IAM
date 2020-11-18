package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.Instant


object CanSampleTable {

  import api._

  /* ------------------------ ORM class definition ------------------------- */

  case class CanSample(
    id: Option[Int],
    canId: Int,
    moment: Instant,
    fillingRate: Double
  ) {

    override def equals(obj: Any): Boolean = obj match {
      case that: CanSample => that.id == this.id
      case _               => false
    }

  }

  /* ----------------------------- Projection ------------------------------ */

  type CanSampleTuple = (Option[Int], Int, Timestamp, Double)

  // Tuple -> CanSample
  private def canSampleApply(t: CanSampleTuple): CanSample =
    CanSample(t._1, t._2, t._3.toInstant, t._4)

  // CanSample -> Tuple
  private def canSampleUnapply(c: CanSample): Option[CanSampleTuple] =
    Some((c.id, c.canId, Timestamp from c.moment, c.fillingRate))

  /* -------------------------- Table definition --------------------------- */

  class CanSamples(tag: Tag) extends Table[CanSample](tag, "can_data") {

    val cans = TableQuery[Cans]

    // Columns
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def canId: Rep[Int] = column[Int]("can_id")

    def moment: Rep[Timestamp] = column[Timestamp]("moment")

    def fillingRate: Rep[Double] = column[Double]("filling_rate")

    // Projection
    def * = (id.?, canId, moment, fillingRate) <> (canSampleApply, canSampleUnapply)

    def can = foreignKey("can_data_ibfk_1", canId, cans)(_.id)

  }

  val canSamples = TableQuery[CanSamples]

}
