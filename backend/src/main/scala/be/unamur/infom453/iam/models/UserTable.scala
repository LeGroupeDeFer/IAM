package be.unamur.infom453.iam.models

import be.unamur.infom453.iam.lib.DBTypes.Hash


object UserTable {

  import api._

  case class User(id: Option[Int], username: String, password: Hash, refreshTokenId: Int)

  class Users(tag: Tag) extends Table[User](tag, "users") {

    // Columns
    def id: Rep[Int]              = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username: Rep[String]     = column[String]("username", O.Unique, O.Length(128))
    def password: Rep[Hash]       = column[Hash]("password", O.Length(128))
    def refreshTokenId: Rep[Int]  = column[Int]("refresh_token_id", O.Unique)

    // Projection
    def * = (id.?, username, password, refreshTokenId) <> (User.tupled, User.unapply)

    // Foreign key
    def refreshToken = foreignKey("refresh_token_fk", refreshTokenId, tokens)(_.id)

  }

  val users = TableQuery[Users]

}

