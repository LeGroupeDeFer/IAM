package be.unamur.infom453.iam

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import wvlet.airframe.http.finagle._
import org.flywaydb.core.Flyway

object Main extends App {

  Configuration.parse(args.toList) match {
    case Some(conf) => run(conf)
    case _          => sys.exit(1)
  }

  def run(conf: Configuration): Unit = conf.mode match {
    case "serve"   => serve(conf)
    case "migrate" => migrate(conf)
    case _         => throw new RuntimeException("Unknown Parameter")
  }

  def serve(conf: Configuration): Unit = {

    val server = Finagle.server
      .withName("IAM")
      .withPort(conf.port)
      .withRouter(controllers.routes)
      .start(server => {
        server.waitServerTermination
        models.db.shutdown
      })

    Await.ready(server, Duration.Inf)
  }

  def migrate(conf: Configuration): Unit = Flyway
      .configure()
      .dataSource(conf.dbUri, conf.dbUser, conf.dbPassword)
      .load()
      .migrate()

}
