package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import be.unamur.infom453.iam.lib._


object TokenTable {

  import api._

  /* ------------------------ ORM class definition ------------------------ */

  case class Token(
    id: Option[Int],
    hash: String,
    creationDate: Instant,
    expirationDate: Option[Instant]
  ) {
    def ttl: Long = expirationDate
      .map(_.getEpochSecond - now.getEpochSecond)
      .getOrElse(0)
  }

  private def tokenTupled(row: (Option[Int], String, Timestamp, Option[Timestamp])): Token =
    Token(row._1, row._2, row._3.toInstant, row._4.map(_.toInstant))

  private def tokenUnapply(t: Token): Option[(Option[Int], String, Timestamp, Option[Timestamp])] =
    Some(t.id, t.hash, Timestamp from t.creationDate, t.expirationDate.map(Timestamp.from))

  class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {

    // Columns
    def id: Rep[Int]                    = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def hash: Rep[String]               = column[String]("hash", O.Length(32), O.Unique)
    def creationDate: Rep[Timestamp]    = column[Timestamp]("creation_date")
    def expirationDate: Rep[Timestamp]  = column[Timestamp]("expiration_date")

    // Projection
    def * = (id.?, hash, creationDate, expirationDate.?) <> (tokenTupled, tokenUnapply)

  }

  val tokens = TableQuery[Tokens]

}

