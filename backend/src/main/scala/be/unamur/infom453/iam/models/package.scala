package be.unamur.infom453.iam

import be.unamur.infom453.iam.Configuration.driver


package object models {

  // Expose everything needed for db manipulations here (to facilitate imports)

  val api = driver.api
  implicit val db = Configuration.database

  type User = be.unamur.infom453.iam.models.UserTable.User
  val users = be.unamur.infom453.iam.models.UserTable.users

  type Can = be.unamur.infom453.iam.models.CanTable.Can
  val cans = be.unamur.infom453.iam.models.CanTable.cans

}
