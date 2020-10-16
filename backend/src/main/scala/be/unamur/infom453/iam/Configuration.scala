package be.unamur.infom453.iam

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import wvlet.airframe.http.finagle.FinagleFilter


// The filter part is not used yet (and may not be used at all)
object Configuration extends FinagleFilter {

  val store: Map[String, String] = {
    val store = Map(
      "DB_HOST"     -> "127.0.0.1",
      "DB_PORT"     -> "3306",
      "DB_DATABASE" -> "iam",
      "DB_USER"     -> "iam",
      "DB_PASSWORD" -> "secret",

      "JWT_SECRET" -> "secret",
      "JWT_ACCESS_LIFETIME" -> (5 * 60).toString,
      "JWT_REFRESH_LIFETIME" -> (14 * 24 * 60 * 60).toString
    ) ++ sys.env

    val host = store("DB_HOST")
    val port = store("DB_PORT")
    val schema = store("DB_DATABASE")

    store + ("DB_URI" -> s"jdbc:mysql://${host}:${port}/${schema}")
  }

  def apply(request: Request, context: Configuration.Context): Future[Response] = {
    context.setThreadLocal("configuration", store)
    context(request)
  }

}
