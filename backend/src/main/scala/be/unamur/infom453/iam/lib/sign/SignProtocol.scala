package be.unamur.infom453.iam.lib.sign

import scala.concurrent.Future


/**
 * Regroup every sign protocol under a common type
 */
trait SignProtocol {

  def code: String

  def verify(pk: String, sign: String, payload: String): Future[Boolean]

  override def toString: String = this.code

  override def equals(obj: Any): Boolean = obj match {
    case that: SignProtocol => that.code == this.code
    case _                  => false
  }

}
