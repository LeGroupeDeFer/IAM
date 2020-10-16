package be.unamur.infom453.iam

import wvlet.airframe.http.finagle._

object Main extends App {

  Finagle.server
    .withName("IAM")
    .withPort(8000)
    .withRouter(controllers.routes)
    .start(server => {
      server.waitServerTermination
      models.db.shutdown
    })

}
