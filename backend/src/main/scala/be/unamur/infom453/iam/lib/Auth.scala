package be.unamur.infom453.iam.lib


import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.matching.Regex
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Future => TwitterFuture}
import wvlet.airframe.http.finagle.FinagleFilter
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import pdi.jwt.algorithms.JwtHmacAlgorithm
import be.unamur.infom453.iam.Configuration.store
import be.unamur.infom453.iam.models._
import be.unamur.infom453.iam.models.UserTable.User


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

  val jwtRefreshLifetime:   Long              = store("JWT_REFRESH_LIFETIME").toLong
  val jwtAccessLifetime:    Long              = store("JWT_ACCESS_LIFETIME").toLong
  val jwtAccessKey:         String            = store("JWT_ACCESS_KEY")
  val jwtAccessAlgorithm:   JwtHmacAlgorithm  = JwtAlgorithm.HS256
  val bearerAuthentication: Regex             = "Bearer (.+)".r

  private def issueAccessToken(subject: String): String = {
    val claim = JwtClaim(issuer = Some("IAM"), subject = Some(subject))
      .startsNow
      .expiresIn(jwtAccessLifetime)
    JwtCirce.encode(claim, jwtAccessKey, jwtAccessAlgorithm)
  }

  /**
   * Given a username and a password, create and save a User.
   *
   * @param username The user name
   * @param password The user password
   * @param ec The execution context on which database IO and futures should be
   *           computed
   * @param db The database on which to operate
   * @return The created user
   */
  def register(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[User] = for {
    token <- TokenTable.create(jwtRefreshLifetime)
    id <- db.run(users += User(None, username, Hash.of(password), token.id.get))
    user <- db.run(users.filter(_.id === id).result.head)
  } yield user

  /**
   * Given a username and a password, verifies that the user exists, that the
   * given password matches the saved hash and returns a refresh token hash on
   * success.
   *
   * @param username The user name
   * @param password The user password
   * @param ec The execution context on which database IO and futures should be
   *           computed
   * @param db The database on which to operate
   * @return The refresh token hash
   */
  def login(username: String, password: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[String] = {
    val retrieval = single(for {
      user <- users if user.username === username
      token <- user.refreshToken
    } yield (user, token))

    for {
      (user, token) <- retrieval if user.password.check(password)
      renewed <- token.renew(jwtRefreshLifetime)
    } yield renewed.hash
  }

  /**
   * Given a username and a refresh token hash, verifies that the token
   * belongs to the user and, if so, make the token expire.
   *
   * @param username The user name
   * @param hash The user refresh token hash
   * @param ec The execution context on which database IO and futures should
   *           be computed
   * @param db The database on which to operate
   * @return Nothing
   */
  def logout(username: String, hash: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[Unit] = for {
    user <- single(users.filter(_.username === username))
    expirationDate = tokens
      .filter(token => token.id === user.refreshTokenId)
      .filter(_.hash === hash)
      .map(_.expirationDate)
    _ <- db.run(expirationDate.update(timestampNow()))
  } yield ()

  /**
   * Given a username and a refresh token hash, verifies that the token
   * belongs to the user and, if so, issue an access token. If the refresh
   * token was about to expire, it is extended.
   *
   * @param username The user name
   * @param hash The refresh token hash
   * @param ec The execution context on which database IO and futures should
   *           be computed
   * @param db The database on which to operate
   * @return The refresh and access token hashes
   */
  def refresh(username: String, hash: String)(
    implicit ec: ExecutionContext,
    db: Database
  ): Future[(String, String)] = {
    val tokenRetrieval = single(for {
      user <- users if user.username === username
      token <- user.refreshToken if token.hash === hash
    } yield token)

    for {
      token <- tokenRetrieval if token.verify(hash) && token.ttl > 0
      refresh <- token.renew(jwtRefreshLifetime)
    } yield (refresh.hash, issueAccessToken(username))
  }

  /* --------------------------- Finagle Filter ---------------------------- */

  private def authenticate(token: String): Try[JwtClaim] = Try {
    val now = timestampNow().getTime / 1000
    val claim = JwtCirce
      .decode(token, jwtAccessKey, Seq(jwtAccessAlgorithm))
      .getOrElse { throw malformedToken }

    if (claim.expiration.getOrElse(0.toLong) <= now)
      throw expiredToken
    else
      claim
  }

  private def authenticated(request: Request): Try[JwtClaim] = Try {
    request.authorization match {
      case Some(bearerAuthentication(token)) => authenticate(token).get
      case None => throw absentToken
    }
  }

  override def apply(request: Request, context: Auth.Context): TwitterFuture[Response] = {
    try {
      val token = authenticated(request).get
      context.setThreadLocal("token", token)
      context(request)
    } catch {
      case _: TokenException => TwitterFuture.value(Response(Status.Unauthorized))
      case e: Throwable => throw e //TwitterFuture.value(Response(Status.InternalServerError))
    }
  }

}
