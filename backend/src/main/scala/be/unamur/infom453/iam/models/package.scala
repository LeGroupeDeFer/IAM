package be.unamur.infom453.iam

import be.unamur.infom453.iam.Configuration.store


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

  type Token = TokenTable.Token
  val tokens = TokenTable.tokens

  type User = UserTable.User
  val users = UserTable.users

  type Can = CanTable.Can
  val cans = CanTable.cans

}
