package be.unamur.infom453.iam.models

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import be.unamur.infom453.iam.lib.ErrorResponse._


object TokenTable {

  import api._

  val seed = Random.alphanumeric
  def nowTimestamp = Timestamp valueOf LocalDateTime.now()
  def afterTimestamp(seconds: Long) = Timestamp valueOf (LocalDateTime.now() plusSeconds seconds)

  case class Token(id: Option[Int], hash: String, creationDate: Timestamp, expirationDate: Option[Timestamp]) {

    def verify(given: String): Boolean = given == hash

    def ttl: Long = expirationDate match {
      case Some(timestamp) => timestamp.getTime - nowTimestamp.getTime
      case None => 0
    }

    def renew(lifetime: Long)(
      implicit ec: ExecutionContext,
      db: Database
    ): Future[Token] = {
      val query = for (
        token <- tokens if token.id === id
      ) yield (token.hash, token.expirationDate)

      val newExpiration = afterTimestamp(lifetime)
      val newHash = (seed take 32) mkString
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

  def create(lifetime: Long)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Token] = {
    // Prepare the data to create a token
    val creationDateTime = LocalDateTime.now()
    val expirationDateTime = creationDateTime plusSeconds lifetime
    val creationDate = Timestamp valueOf creationDateTime
    val expirationDate = Timestamp valueOf expirationDateTime
    val hash: String = seed take 32 mkString

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

