package be.unamur.infom453.iam

import cats.effect._
import cats.effect.internals.IOContextShift
import com.twitter.finagle.{Http}
import com.twitter.util.Await


object Main extends scala.App {

  implicit def contextShift: ContextShift[IO] = IOContextShift.global

  val app = new App()
  Await.ready(Http.server.serve(":8000", app.service))

}
