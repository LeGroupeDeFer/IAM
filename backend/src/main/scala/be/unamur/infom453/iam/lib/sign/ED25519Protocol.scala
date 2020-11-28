package be.unamur.infom453.iam.lib.sign

import scala.concurrent.Future
import java.security.{KeyFactory, KeyPairGenerator, PublicKey, Security, Signature}

import org.apache.commons.codec.binary.Base64
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import net.i2p.crypto.eddsa.spec.{EdDSANamedCurveTable, EdDSAPublicKeySpec}
import be.unamur.infom453.iam.lib._


case object ED25519Protocol extends SignProtocol {

  Security.addProvider(new EdDSASecurityProvider())

  val code = "ed25519"

  private[iam] val signature = Signature.getInstance("NONEwithEdDSA", "EdDSA")
  private[iam] val keyFactory = KeyFactory.getInstance("EdDSA", "EdDSA")
  private[iam] val generator  = KeyPairGenerator.getInstance("EdDSA")

  def verify(pk: String, b64cipher: String, plainText: String): Future[Boolean] = Future {
    signature.initVerify(publicKeyFromString(pk))
    signature.update(plainText.getBytes("UTF-8"))

    val cipher = Base64.decodeBase64(b64cipher)

    if (!signature.verify(cipher))
      throw invalidSignature
    else
      true
  }

  private def publicKeyFromString(pub: String): PublicKey = {
    val bytes = Base64.decodeBase64(pub.replace("\n", ""))
    val ed25519 = EdDSANamedCurveTable.getByName("Ed25519")
    val spec = new EdDSAPublicKeySpec(bytes, ed25519)
    keyFactory.generatePublic(spec)
  }

}
