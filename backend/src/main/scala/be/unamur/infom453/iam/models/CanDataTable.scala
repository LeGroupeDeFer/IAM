package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.Instant

import be.unamur.infom453.iam.models.CanTable.Cans
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.ProvenShape

object CanDataTable {

  import api._

  /* ------------------------ ORM class definition ------------------------ */

  case class CanData(id: Option[Int], can_id: Int, moment: Instant, filling_rate: Double)

  def canDataUnapply(c: CanData) =
    Some((c.id, c.can_id, Timestamp from c.moment, c.filling_rate))


  def canDataTupled(t: (Option[Int], Int, Timestamp, Double)) = {
    CanData(t._1, t._2, t._3.toInstant, t._4)
  }

  class CanDatas(tag: Tag) extends Table[CanData](tag, "can_data") {

    val cans = TableQuery[Cans]

    // Columns
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def can_id: Rep[Int] = column[Int]("can_id")

    def moment: Rep[Timestamp] = column[Timestamp]("moment")

    def filling_rate: Rep[Double] = column[Double]("filling_rate")

    // Projection
    def * = (id.?, can_id, moment, filling_rate) <> (canDataTupled, canDataUnapply)

    def can =
      foreignKey("can_data_ibfk_1", can_id, cans)(_.id)

  }

  val canDatas = TableQuery[CanDatas]

  /* --------------------- ORM Manipulation functions --------------------- */


  implicit class CanDataExtension[C[_]](q: Query[CanTable.Cans, CanTable.Can, C]) {
    // specify mapping of relationship to address
    def withData =
      q.joinLeft(CanDataTable.canDatas).on(_.id === _.can_id)
  }

}


