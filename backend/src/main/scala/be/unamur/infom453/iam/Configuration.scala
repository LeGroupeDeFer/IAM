package be.unamur.infom453.iam

import scopt.OParser


case class Configuration(
  mode: String = "serve",
  port: Int = 8000,
  // Database
  dbHost: String = "127.0.0.1",
  dbPort: Int = 3306,
  dbSchema: String = "iam",
  dbUser: String = "iam",
  dbPassword: String = "secret",
  dbSSL: Boolean = false,
  // Auth
  authAccessKey: String = "secret",
  authAccessLifetime: Int = 5 * 60,
  authRefreshLifetime: Int = 14 * 24 * 60 * 60
) {

  require(authAccessLifetime > 0)
  require(authRefreshLifetime > authAccessLifetime)

  def dbUri: String =
    s"jdbc:mysql://${dbHost}:${dbPort}/${dbSchema}?useSSL=${dbSSL}"

}


object Configuration {

  private var current: Option[Configuration] = None
  private val builder = OParser.builder[Configuration]
  import builder._

  private val dbOpts = Seq(
    opt[String]("database-host")
      .action((h, c) => c.copy(dbHost=h))
      .text("The host on which the database must be contacted"),
    opt[Int]("database-port")
      .action((p, c) => c.copy(dbPort=p))
      .text("The port on which the database is listening"),
    opt[String]('s', "database-schema")
      .action((s, c) => c.copy(dbSchema=s))
      .text("The schema to use during database session"),
    opt[String]('u', "database-user")
      .action((u, c) => c.copy(dbUser=u))
      .text("The user name used to establish the database session"),
    opt[String]("database-password")
      .action((p, c) => c.copy(dbPassword=p))
      .text("The user password used to establish the database session")
  )

  private val authOpts = Seq(
    opt[String]('k', "auth-key")
      .action((k, c) => c.copy(authAccessKey = k))
      .text("The HS256 key used for JWT encryption"),
    opt[Int]('l', "token-lifetime")
      .action((l, c) => c.copy(authAccessLifetime = l))
      .text("The JWT token lifetime, in seconds"),
    opt[Int]("session-lifetime")
      .action((l, c) => c.copy(authRefreshLifetime = l))
      .text(
        "The session lifetime during which JWT renewal is authorized." +
          "Must be greater than jwt token lifetime."
      )
  )

  private val parser = OParser.sequence(
    programName("iam"),
    head("iam", "0.1.0"),
    cmd("serve")
      .action((_, c) => c.copy(mode="serve"))
      .text("Start the IAM web server")
      .children(Seq(
        opt[Int]('p', "port")
          .action((p, c) => c.copy(port = p))
          .text("The port on which the server shall listen for incoming requests")
      ) ++ dbOpts ++ authOpts: _*),
    cmd("migrate")
      .action((_, c) => c.copy(mode="migration"))
      .text("Update the database schema to the latest version")
      .children(dbOpts: _*)
  )

  private def fromEnv: Configuration = {
    type C = Configuration
    val env = Seq(
      "DB_HOST"     -> ((c: C, v: String) => c.copy(dbHost = v)),
      "DB_PORT"     -> ((c: C, v: String) => c.copy(dbPort = v.toInt)),
      "DB_DATABASE" -> ((c: C, v: String) => c.copy(dbSchema = v)),
      "DB_USER"     -> ((c: C, v: String) => c.copy(dbUser = v)),
      "DB_PASSWORD" -> ((c: C, v: String) => c.copy(dbPassword = v)),
      "DB_SSL"      -> ((c: C, v: String) => c.copy(dbSSL = if (v == "true") true else false)),

      "AUTH_ACCESS_KEY"       -> ((c: C, v: String) => c.copy(authAccessKey = v)),
      "AUTH_ACCESS_LIFETIME"  -> ((c: C, v: String) => c.copy(authAccessLifetime = v.toInt)),
      "AUTH_REFRESH_LIFETIME" -> ((c: C, v: String) => c.copy(authRefreshLifetime = v.toInt))
    )

    env.map(kv => (kv._2, sys.env.get(kv._1))).foldLeft(Configuration()) {
      case (c, (f, Some(v))) => f(c, v)
      case (c, _)            => c
    }
  }

  def parse(args: List[String]): Option[Configuration] = {
    current = OParser.parse(parser, args, fromEnv)
    current
  }

  def instance: Option[Configuration] = current

}
