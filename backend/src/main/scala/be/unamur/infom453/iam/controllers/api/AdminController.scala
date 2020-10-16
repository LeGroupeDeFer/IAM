package be.unamur.infom453.iam.controllers.api

import be.unamur.infom453.iam.lib.Auth
import wvlet.airframe.http.{Endpoint, HttpMethod, Router}


object AdminController {

  val routes = Router
    .add(Auth)
    .andThen[AdminController]

}

// These endpoints are behind the auth
@Endpoint(path="/api/admin")
trait AdminController {

  @Endpoint(method=HttpMethod.GET, path="/noodles")
  def noodles = "Don't look now, but there is a multi-legged creature on your shoulder."
  
}
