package be.unamur.infom453.iam

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}

import com.twitter.{util => twitter}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}


package object lib {

  /* --------------------- Global & global implicits ---------------------- */

  implicit val ec: ExecutionContext = global
  implicit val api = models.api // TODO Annotation
  implicit val db: models.api.Database = models.db
  implicit val zoneOffset: ZoneOffset = ZoneOffset.UTC

  val insertionError    = new Exception("Unable to insert")
  val updateError       = new Exception("Unable to update")
  val persistenceError  = new Exception("Entity was not saved to the database yet")
  val invalidPassword   = new Exception("Invalid password")
  val invalidIDs        = new Exception("Invalid authentication IDs")
  val invalidToken      = new Exception("Invalid token")
  val usernameTaken     = new Exception("Username in use")

  /* -------------------------- Utils functions --------------------------- */

  import api._

  def timestampNow(): Timestamp =
    Timestamp.valueOf(LocalDateTime.now(zoneOffset))

  def timestampAfter(seconds: Long): Timestamp =
    Timestamp.valueOf(LocalDateTime.now(zoneOffset) plusSeconds seconds)

  def singleOption[A, B](query: Query[A, B, Seq])(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Option[B]] = db.run(query.result.headOption)

  def single[A, B](query: Query[A, B, Seq])(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[B] = db.run(query.result.head)

  /* ------------------------ Implicit conversions ------------------------ */

  // ScalaTry[T] -> TwitterTry[T]
  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r) => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  // TwitterTry[T] -> ScalaTry[T]
  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  // ScalaFuture[T] -> TwitterFuture[T]
  implicit def scalaToTwitterFuture[T](f: Future[T])(
    implicit ec: ExecutionContext
  ): twitter.Future[T] = {
    val promise = twitter.Promise[T]()
    f.onComplete(promise update _)(ec)
    promise
  }

  // TwitterFuture[T] -> ScalaFuture[T]
  implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    f.respond(promise complete _)
    promise.future
  }

  /* -------------------------- Database related -------------------------- */

  object Hash {
    def of(pw: String) = Hash(BCrypt.hashpw(pw, BCrypt.gensalt()))
  }

  case class Hash(value: String) extends MappedTo[String] {
    def check(password: String): Boolean =
      BCrypt.checkpw(password, value)
  }

}
