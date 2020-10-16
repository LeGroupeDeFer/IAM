package be.unamur.infom453.iam.models


object CanTable {

  import api._

  case class Can(id: Option[Int], latitude: Double, longitude: Double, publicKey: String)

  class Cans(tag: Tag) extends Table[Can](tag, "cans") {

    // Columns
    def id: Rep[Int]            = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def latitude: Rep[Double]   = column[Double]("latitude")
    def longitude: Rep[Double]  = column[Double]("longitude")
    def publicKey: Rep[String]  = column[String]("public_key", O.Length(2048))

    // Projection
    def * = (id.?, latitude, longitude, publicKey) <> (Can.tupled, Can.unapply)

  }

  val cans = TableQuery[Cans]

}
