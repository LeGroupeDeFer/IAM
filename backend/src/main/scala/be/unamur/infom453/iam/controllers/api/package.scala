package be.unamur.infom453.iam.controllers

import wvlet.airframe.http.Router

package object api {
  val routes: Router = Router(
    APIController.routes,
    AuthController.routes,
    AdminController.routes
  )
}
