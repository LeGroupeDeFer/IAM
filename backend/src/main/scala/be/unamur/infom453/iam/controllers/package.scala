package be.unamur.infom453.iam

import wvlet.airframe.http.Router

package object controllers {
  val routes: Router = Router(api.routes, StaticController.routes)
}
