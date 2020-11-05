package be.unamur.infom453.iam.lib

package object sign {


  /**
   * Regroup every sign protocol under a common type
   */
  class SignProtocol {
    def code: String = "none"
  }

  case object NoneProtocol extends SignProtocol

  case object RSAProtocol extends SignProtocol {
    override def code: String = "rsa"
  }

  val availableProtocols: Seq[SignProtocol] = Seq(RSAProtocol, NoneProtocol)

}
