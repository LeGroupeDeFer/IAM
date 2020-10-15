package be.unamur.infom453.iam.controllers

import wvlet.airframe.http.{Endpoint, Router, StaticContent}


object Static {
  val routes: Router = Router.add[Static]
}

trait Static {

  @Endpoint(path="/")
  def index() = content("index.html")

  @Endpoint(path="/*path")
  def content(path: String) = StaticContent.fromResource(basePath="", path)

}
