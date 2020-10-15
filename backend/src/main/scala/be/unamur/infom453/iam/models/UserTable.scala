package be.unamur.infom453.iam.models

import scala.util.{Failure, Success, Try}
import be.unamur.infom453.iam.Configuration.driver
import be.unamur.infom453.iam.lib.Auth.invalidAuth


object UserTable {

  import driver.api._

  case class User(id: Option[Int], username: String, password: String) {
    // TODO - Implement BCrypt verification and encoding
    def verify(given: String): Try[User] =
      if (given == password) Success(this)
      else Failure(invalidAuth)
  }

  class Users(tag: Tag) extends Table[User](tag, "users") {

    // Columns
    def id: Rep[Int]          = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username: Rep[String] = column[String]("username", O.Unique, O.Length(128))
    def password: Rep[String] = column[String]("password", O.Length(128))

    // Select
    def * = (id.?, username, password) <> (User.tupled, User.unapply)

  }

  val users = TableQuery[Users]

}

