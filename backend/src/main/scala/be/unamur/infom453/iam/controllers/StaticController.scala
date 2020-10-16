package be.unamur.infom453.iam.controllers

import wvlet.airframe.http.{Endpoint, Router, StaticContent}


object StaticController {
  val routes: Router = Router.of[StaticController]
}

trait StaticController {

  @Endpoint(path="/")
  def index() = content("index.html")

  @Endpoint(path="/*path")
  def content(path: String) = StaticContent.fromResource(basePath="", path)

}
