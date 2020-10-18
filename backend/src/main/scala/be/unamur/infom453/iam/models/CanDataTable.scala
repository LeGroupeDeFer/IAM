package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.Instant

object CanDataTable {

  import api._

  /* ------------------------ ORM class definition ------------------------ */

  case class CanData(id: Option[Int], canId: Int, moment: Instant, fillingRate: Double)

  private def canDataTupled(t: (Option[Int], Int, Timestamp, Double)): CanData = {
    CanData(t._1, t._2, t._3.toInstant, t._4)
  }

  private def canDataUnapply(c: CanData): Option[(Option[Int], Int, Timestamp, Double)] =
    Some((c.id, c.canId, Timestamp from c.moment, c.fillingRate))

  class CanDatas(tag: Tag) extends Table[CanData](tag, "can_data") {

    val cans = TableQuery[Cans]

    // Columns
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def canId: Rep[Int] = column[Int]("can_id")

    def moment: Rep[Timestamp] = column[Timestamp]("moment")

    def fillingRate: Rep[Double] = column[Double]("filling_rate")

    // Projection
    def * = (id.?, canId, moment, fillingRate) <> (canDataTupled, canDataUnapply)

    def can =
      foreignKey("can_data_ibfk_1", canId, cans)(_.id)

  }

  val canDatas = TableQuery[CanDatas]

  /* --------------------- ORM Manipulation functions --------------------- */

}


