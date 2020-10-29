package be.unamur.infom453.iam.models

import java.time.Clock

import be.unamur.infom453.iam.lib.{now, timestampNow, timestampAfter, after}
import be.unamur.infom453.iam.models.TokenTable.Token

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object extensions {

  import api._
  import slick.dbio.DBIOAction
  import slick.dbio.NoStream

  implicit class SqlActionExtension[+R, +S <: slick.dbio.NoStream, -E <: slick.dbio.Effect](
                                                                                             a: DBIOAction[R, S, E]
                                                                                           ) {

    def execute(implicit ec: ExecutionContext, db: Database): Future[R] =
      db.run(a)

  }

  // All Extension

  implicit class TableExtension[As, A, C[_]](q: Query[As, A, C]) {

    def one(implicit ec: ExecutionContext, db: Database): Future[A] =
      db.run(q.result.head)

    def oneOption(implicit ec: ExecutionContext, db: Database): Future[Option[A]] =
      db.run(q.result.headOption)

    def execute(implicit ec: ExecutionContext, db: Database): Future[C[A]] =
      db.run(q.result)

  }

  // User Extension

  implicit class UserExtension[C[_]](q: Query[Users, User, C]) {

    def withId(id: Int): Query[Users, User, C] =
      q.filter(_.id === id)

    def withUsername(username: String): Query[Users, User, C] =
      q.filter(_.username === username)

    def withToken: Query[(Users, Tokens), (User, Token), C] =
      q.join(tokens).on(_.refreshTokenId === _.id)

    def token: Query[Tokens, Token, C] = q.withToken.map(_._2)


    def insert(user: User): DBIOAction[Int, NoStream, Effect.Write] =
      users returning users.map(_.id) += user

  }

  // Token Extension

  implicit class TokenExtension[C[_]](q: Query[Tokens, Token, C]) {

    private def seed: String = Random.alphanumeric.take(32).mkString

    def withId(id: Int): Query[Tokens, Token, C] =
      q.filter(_.id === id)

    def withHash(hash: String): Query[Tokens, Token, C] =
      q.filter(_.hash === hash)

    def ifNotExpired: Query[Tokens, Token, C] =
      q.filter(_.expirationDate >= timestampNow())


    def insert(lifetime: Long)(implicit clock: Clock): DBIOAction[Int, NoStream, Effect.Write] =
      tokens returning tokens.map(_.id) += Token(None, seed, now, Some(after(Math.max(0, lifetime))))

    def revoke: DBIOAction[Int, NoStream, Effect.Write] =
      q.map(_.expirationDate).update(timestampNow())

    def renew(hash: String, lifetime: Long)(implicit clock: Clock): DBIOAction[Int, NoStream, Effect.Write] =
      q.map(t => (t.hash, t.expirationDate)).update((hash, timestampAfter(lifetime)))

  }

  // Can Extension

  implicit class CanExtension[C[_]](q: Query[Cans, Can, C]) {

    // specify mapping of relationship to address
    def active: Query[Cans, Can, C] =
      q.filter(_.deletedAt.isEmpty)

    def withIdentifier(identifier: String): Query[Cans, Can, C] =
      q.filter(_.identifier === identifier)

    def withId(id: Int): Query[Cans, Can, C] =
      q.filter(_.id === id)

    def withData =
      q.joinLeft(canDatas).on(_.id === _.canId)

    def insert(can: Can): DBIOAction[Int, NoStream, Effect.Write] =
      cans returning cans.map(_.id) += can

  }

  // Can Data Extension

  // TODO

}
