package be.unamur.infom453.iam.lib


package object sign {

  val availableProtocols: Seq[SignProtocol] = Seq(
    RSAProtocol,
    ED25519Protocol,
    NoneProtocol
  )

  def from(code: String): Option[SignProtocol] =
    availableProtocols.find(_.code == code)

}
