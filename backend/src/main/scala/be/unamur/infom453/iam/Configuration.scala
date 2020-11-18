package be.unamur.infom453.iam

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import wvlet.airframe.http.finagle.FinagleFilter


// The filter part is not used yet (and may not be used at all)
object Configuration extends FinagleFilter {

  val store: Map[String, String] = {
    val defaults = Map(
      "DB_HOST"     -> "127.0.0.1",
      "DB_PORT"     -> "3306",
      "DB_DATABASE" -> "iam",
      "DB_USER"     -> "iam",
      "DB_PASSWORD" -> "secret",
      "DB_SSL"      -> "false",

      "JWT_ACCESS_KEY" -> "secret",
      "JWT_ACCESS_LIFETIME" -> (5 * 60).toString,
      "JWT_REFRESH_LIFETIME" -> (14 * 24 * 60 * 60).toString
    ) ++ sys.env

    val host = defaults("DB_HOST")
    val port = defaults("DB_PORT")
    val ssl = defaults("DB_SSL")
    val schema = defaults("DB_DATABASE")

    defaults + ("DB_URI" -> s"jdbc:mysql://${host}:${port}/${schema}?useSSL=${ssl}")
  }

  // Not used yet
  def apply(request: Request, context: Configuration.Context): Future[Response] = {
    context.setThreadLocal("configuration", store)
    context(request)
  }

}
