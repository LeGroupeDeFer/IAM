package be.unamur.infom453.iam.lib


import be.unamur.infom453.iam.models.UserTable.User
import be.unamur.infom453.iam.models._
import be.unamur.infom453.iam.Configuration
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Future => TwitterFuture}
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import wvlet.airframe.http.finagle.FinagleFilter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.{Random, Try}


/**
 * The Auth object represents a dual token authentication. The first token,
 * the '''refresh token''', is delivered on login and is meant to be long
 * lived. Its sole purpose is to permit the user to request a short lived
 * '''access token'''. The access token is a '''JSON Web Token''' claim,
 * meant to be used for resource protection and identity validation.
 */
object Auth extends FinagleFilter {

  /* TODO Find a simple way to add users */

  import api._
  import Ops._
  private val conf = Configuration.instance.get
  val jwtAlgorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
  val bearerAuthentication: Regex = "Bearer (.+)".r

  /**
   * Given a username and a password, create and save a User.
   *
   * @param username The user name
   * @param password The user password
   * @param ec       The execution context on which database IO and futures should be
   *                 computed
   * @param db       The database on which to operate
   * @return The created user
   */
  def register(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[User] = {
    val hash = Hash.of(password)
    for {
      tokenId <- tokens.insert(0).execute
      userId <- users.insert(User(None, username, hash, tokenId)).execute
    } yield User(Some(userId), username, hash, tokenId)
  }

  /**
   * Given a username and a password, verifies that the user exists, that the
   * given password matches the saved hash and returns a refresh token hash on
   * success.
   *
   * @param username The user name
   * @param password The user password
   * @param ec       The execution context on which database IO and futures should be
   *                 computed
   * @param db       The database on which to operate
   * @return The refresh token hash
   */
  def login(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[String] = try {
    val refreshHash = seed
    for {
      user <- users.withUsername(username).one if user.password == password
      _ <- tokens.withId(user.refreshTokenId).renew(refreshHash, conf.authRefreshLifetime).execute
      hash <- tokens.withId(user.refreshTokenId).map(_.hash).one
    } yield hash
  } catch {
    case _: Exception => throw invalidIDs
  }

  /**
   * Given a username and a refresh token hash, verifies that the token
   * belongs to the user and, if so, make the token expire.
   *
   * @param username The user name
   * @param hash     The user refresh token hash
   * @param ec       The execution context on which database IO and futures should
   *                 be computed
   * @param db       The database on which to operate
   * @return true if the user was disconnected, false otherwise
   */
  def logout(username: String, hash: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Unit] = for {
    user <- users.withUsername(username).one
    modified <- tokens.withId(user.refreshTokenId).withHash(hash).ifNotExpired.revoke.execute
  } yield
    if (modified < 1) throw invalidToken
    else ()

  /**
   * Given a username and a refresh token hash, verifies that the token
   * belongs to the user and, if so, issue an access token. If the refresh
   * token was about to expire, it is extended.
   *
   * @param username The user name
   * @param hash     The refresh token hash
   * @param ec       The execution context on which database IO and futures should
   *                 be computed
   * @param db       The database on which to operate
   * @return The refresh and access token hashes
   */
  def refresh(username: String, hash: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[(String, String)] = for {
    user <- users.withUsername(username).one
    refreshHash = seed
    modified <- tokens
      .withId(user.refreshTokenId)
      .withHash(hash)
      .ifNotExpired
      .renew(refreshHash, conf.authRefreshLifetime)
      .execute
  } yield
    if (modified < 1) throw invalidToken
    else (refreshHash, issueAccessToken(username))

  private def seed: String = Random.alphanumeric.take(32).mkString

  private def issueAccessToken(subject: String): String = {
    val claim = JwtClaim(issuer = Some("IAM"), subject = Some(subject))
      .startsNow
      .expiresIn(conf.authAccessLifetime)
    JwtCirce.encode(claim, conf.authAccessKey, jwtAlgorithm)
  }

  /* --------------------------- Finagle Filter ---------------------------- */

  override def apply(request: Request, context: Auth.Context): TwitterFuture[Response] = {
    try {
      val token = authenticated(request).get
      context.setThreadLocal("token", token)
      context(request)
    } catch {
      case _: TokenException => TwitterFuture.value(Response(Status.Unauthorized))
      // TODO Add the throwable body to the response
      case _: Throwable => TwitterFuture.value(Response(Status.InternalServerError))
    }
  }

  private def authenticated(request: Request): Try[JwtClaim] = Try {
    request.authorization match {
      case Some(bearerAuthentication(token)) => authenticate(token).get
      case None => throw absentToken
    }
  }

  private def authenticate(token: String): Try[JwtClaim] = Try {
    JwtCirce
      .decode(token, conf.authAccessKey, Seq(jwtAlgorithm))
      .getOrElse {
        throw expiredToken
      }
  }

}
