package be.unamur.infom453.iam

import wvlet.airframe.http.finagle._

object Main extends App {

  def store(): Map[String, String] = Map(
    "DB_HOST"     -> "127.0.0.1",
    "DB_PORT"     -> "3306",
    "DB_DATABASE" -> "iam",
    "DB_USER"     -> "iam",
    "DB_PASSWORD" -> "secret"
  ) ++ sys.env

  Finagle.server
    .withName("IAM")
    .withPort(8000)
    .withRouter(controllers.routes)
    .start(server => {
      server.waitServerTermination
      Configuration.database.shutdown
    })

}
