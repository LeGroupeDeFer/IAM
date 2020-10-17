package be.unamur.infom453.iam.controllers

import wvlet.airframe.http.HttpMessage.Response
import wvlet.airframe.http.{Endpoint, Router, StaticContent}
import be.unamur.infom453.iam.lib.jarDirectory

object StaticController {

  val routes: Router = Router.of[StaticController]
  Console.err.println(s"$jarDirectory")
  val asset: StaticContent = StaticContent.fromDirectory(s"$jarDirectory/assets")

}

trait StaticController {

  import StaticController.asset

  @Endpoint(path="/")
  def index(): Response =
    content("index.html")

  @Endpoint(path="/*path")
  def content(path: String): Response =
    asset(path)

}
