package be.unamur.infom453.iam.lib.sign

import scala.concurrent.Future


case object NoneProtocol extends SignProtocol {

  val code = "none"

  def verify(pk: String, sign: String, payload: String): Future[Boolean] =
    Future.successful(true)

}
