package be.unamur.infom453.iam.lib

import scala.util.{Failure, Success, Try}
import scala.concurrent.{ExecutionContext, Future}
import be.unamur.infom453.iam.models._
import be.unamur.infom453.iam.Implicits.api._

object Auth {

  val invalidAuth = new Exception("Invalid authentication IDs")

  // TODO - Return a refresh token
  def login(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Try[User]] = {
    val query = for (u <- users if u.username === username) yield u
    for (u <- db.run(query.result.headOption)) yield u match {
      case Some(user) => user.verify(password)
      case None => Failure(invalidAuth)
    }
  }

  def logout(username: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Try[User]] = {
    // TODO - Actual logout
    val query = for (u <- users if u.username === username) yield u
    for (u <- db.run(query.result.headOption)) yield u match {
      case Some(user) => Success(user)
      case None => Failure(invalidAuth)
    }
  }

  // TODO - Take a refresh token, return an access token
  def refresh(token: String) = ???

}
