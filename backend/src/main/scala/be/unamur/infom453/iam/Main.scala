package be.unamur.infom453.iam

import java.net.URLConnection
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import wvlet.airframe.http.finagle._


object Main extends App {

  // TODO - flags

  val server = Finagle.server
    .withName("IAM")
    .withPort(8000)
    .withRouter(controllers.routes)
    .start(server => {
      server.waitServerTermination
      models.db.shutdown
    })

  Await.ready(server, Duration.Inf)

}
