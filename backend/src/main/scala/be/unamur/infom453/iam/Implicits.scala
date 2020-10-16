package be.unamur.infom453.iam

import com.twitter.{util => twitter}
import scala.concurrent.{ExecutionContext, Promise, Future}
import language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}


object Implicits {

  implicit val ec = global
  implicit val api = models.api
  implicit val db = models.db

  /*
   * Set of conversions meant to ease the translation from Twitter/Scala
   * Futures to Scala/Twitter Futures
   */

  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r) => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  implicit def scalaToTwitterFuture[T](f: Future[T])(implicit ec: ExecutionContext): twitter.Future[T] = {
    val promise = twitter.Promise[T]()
    f.onComplete(promise update _)(ec)
    promise
  }

  implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    f.respond(promise complete _)
    promise.future
  }

}
