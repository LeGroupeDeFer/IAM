package be.unamur.infom453.iam

import java.sql.Timestamp

import be.unamur.infom453.iam.Configuration.store
import be.unamur.infom453.iam.lib.Hash
import be.unamur.infom453.iam.lib.timestampNow

import scala.concurrent.{ExecutionContext, Future}


package object models {

  // Expose everything needed for db manipulations here (to facilitate imports)

  implicit val api = slick.jdbc.MySQLProfile.api
  import api._

  implicit val db = Database.forURL(
    store("DB_URI"),
    store("DB_USER"),
    store("DB_PASSWORD"),
    driver="com.mysql.cj.jdbc.Driver"
  )

  type Users = UserTable.Users
  type User = UserTable.User
  val users = UserTable.users

  type Tokens = TokenTable.Tokens
  type Token = TokenTable.Token
  val tokens = TokenTable.tokens

  type Cans = CanTable.Cans
  type Can = CanTable.Can
  val cans = CanTable.cans

  // There's an english mistake, data is not countable and doesn't have a plural form
  type CanDatas = CanDataTable.CanDatas
  type CanData = CanDataTable.CanData
  val canDatas = CanDataTable.canDatas

}
