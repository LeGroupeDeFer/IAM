package be.unamur.infom453.iam.controllers.api

import wvlet.airframe.http.{Endpoint, HttpMethod, Router}

object APIController {

  val routes: Router = Router.of[APIController]

}

@Endpoint(path="/api")
trait APIController {

  @Endpoint(method=HttpMethod.GET, path = "/noodles")
  def noodles() = "noodles"

}

