package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.LocalDateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import be.unamur.infom453.iam.lib._


object TokenTable {

  val seed = Random.alphanumeric.take(32).mkString
  import api._

  /* ------------------------ ORM class definition ------------------------ */

  case class Token(
    id: Option[Int],
    hash: String,
    creationDate: Timestamp,
    expirationDate: Option[Timestamp]
  ) {
    def verify(given: String): Boolean =
      given == hash

    def ttl: Long = expirationDate
      .map(_.getTime - timestampNow().getTime)
      .getOrElse(0)

    def renew(lifetime: Long)(
      implicit ec: ExecutionContext,
      db: Database
    ): Future[Token] = {
      if (this.ttl > lifetime / 2)
        return Future(this)

      val query = for (
        token <- tokens if token.id === id
      ) yield (token.hash, token.expirationDate)

      val newExpiration = timestampAfter(lifetime)
      val newHash = seed
      val update = query.update((newHash, newExpiration))

      db.run(update)
        .map(_ => Token(id, newHash, creationDate, Some(newExpiration)))
        .recoverWith { case _: Exception => throw updateError}
    }
  }

  class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {

    // Columns
    def id: Rep[Int]                    = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def hash: Rep[String]               = column[String]("hash", O.Length(32), O.Unique)
    def creationDate: Rep[Timestamp]    = column[Timestamp]("creation_date")
    def expirationDate: Rep[Timestamp]  = column[Timestamp]("expiration_date")

    // Projection
    def * = (id.?, hash, creationDate, expirationDate.?) <> (Token.tupled, Token.unapply)

  }

  val tokens = TableQuery[Tokens]

  /* --------------------- ORM Manipulation functions --------------------- */

  def create(lifetime: Long)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Token] = {
    // Prepare the data to create a token
    val creationDateTime = LocalDateTime.now()
    val expirationDateTime = creationDateTime plusSeconds lifetime
    val creationDate = Timestamp valueOf creationDateTime
    val expirationDate = Timestamp valueOf expirationDateTime
    val hash: String = seed

    // Create a token
    val token = Token(None, hash, creationDate, Some(expirationDate))

    // Insert
    for {
      id <- db.run(tokens += token)
      insert <- db.run(tokens.filter(_.id === id).result.headOption)
    } yield insert match {
      case Some(token) => token
      case None => throw insertionError
    }
  }

}

