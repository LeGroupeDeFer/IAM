package be.unamur.infom453.iam.lib

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

import scala.concurrent.{ExecutionContext, Future}
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import be.unamur.infom453.iam.models._
import be.unamur.infom453.iam.Implicits.api._
import be.unamur.infom453.iam.lib.DBTypes.Hash
import be.unamur.infom453.iam.models.UserTable.User
import be.unamur.infom453.iam.Configuration.store
import be.unamur.infom453.iam.lib.ErrorResponse._


object Auth {

  def register(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[User] = for {
    token <- TokenTable.create(store("JWT_REFRESH_LIFETIME").toLong) // TODO Parsing should be done somewhere else
    id <- db.run(users += User(None, username, Hash.of(password), token.id.get))
    user <- db.run(users.filter(_.id === id).result.headOption)
  } yield user match {
    case Some(user) => user
    case None => throw usernameTaken
  }

  def login(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[String] = {
    val refreshLifetime = store("JWT_REFRESH_LIFETIME").toLong

    val query = for {
      user <- users if user.username === username
      token <- user.refreshToken
    } yield (user, token)

    val tokenQuery = for (result <- db.run(query.result.headOption)) yield result match {
      case Some((user, token)) => {
        if (user.password.check(password)) token
        else throw invalidAuth
      }
      case None => throw invalidAuth
    }

    tokenQuery flatMap (t => t.renew(refreshLifetime)) map (_.hash)
  }

  def logout(username: String, hash: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Unit] = {
    val query = for {
      token <- tokens if token.hash === hash
    } yield token.expirationDate

    val update = query.update(Timestamp valueOf LocalDateTime.now())

    db.run(update)
      .map(_ => ())
      .recover { case _: Exception => throw invalidAuth }
  }

  def refresh(username: String, hash: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[String] = {
    val refreshLifetime = store("JWT_REFRESH_LIFETIME").toLong
    val accessLifetime = store("JWT_ACCESS_LIFETIME").toLong
    val key = store("JWT_SECRET")
    val algo = JwtAlgorithm.HS256

    val query = for {
      user <- users if user.username === username
      refreshToken <- user.refreshToken
    } yield (user, refreshToken)

    val tokenVerification = for (result <- db.run(query.result.headOption)) yield result match {
      case Some((_, refreshToken)) =>
        if (refreshToken.verify(hash) && refreshToken.ttl > 0) refreshToken
        else throw invalidToken
      case None => throw invalidToken
    }

    for (refreshToken <- tokenVerification) yield {
      // TODO Use the renewed token
      if (refreshToken.ttl < refreshLifetime / 2)
        refreshToken.renew(refreshLifetime)
      val claim = JwtClaim(
        issuer = Some("IAM"),
        expiration = Some(Instant.now.plusSeconds(accessLifetime).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond)
      )
      JwtCirce.encode(claim, key, algo)
    }
  }

}
