package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.Instant

import be.unamur.infom453.iam.lib._


object TokenTable {

  import api._

  /* ------------------------ ORM class definition ------------------------- */

  case class Token(
    id: Option[Int],
    hash: String,
    creationDate: Instant,
    expirationDate: Option[Instant]
  ) {

    def ttl: Long = expirationDate
      .map(_.getEpochSecond - now.getEpochSecond)
      .getOrElse(0)

    override def equals(obj: Any): Boolean = obj match {
      case that: Token => that.id == this.id
      case _           => false
    }

  }

  /* ----------------------------- Projection ----------------------------- */

  type TokenTuple = (Option[Int], String, Timestamp, Option[Timestamp])

  // Tuple -> Token
  private def tokenApply(t: TokenTuple): Token =
    Token(t._1, t._2, t._3.toInstant, t._4.map(_.toInstant))

  // Token -> Tuple
  private def tokenUnapply(t: Token): Option[TokenTuple] = Some(
    t.id, t.hash, Timestamp from t.creationDate, t.expirationDate.map(Timestamp.from)
  )

  /* -------------------------- Table definition --------------------------- */

  class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {

    // Columns
    def id: Rep[Int]                    = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def hash: Rep[String]               = column[String]("hash", O.Length(32), O.Unique)
    def creationDate: Rep[Timestamp]    = column[Timestamp]("creation_date")
    def expirationDate: Rep[Timestamp]  = column[Timestamp]("expiration_date")

    // Projection
    def * = (id.?, hash, creationDate, expirationDate.?) <> (tokenApply, tokenUnapply)

  }

  val tokens = TableQuery[Tokens]

}
