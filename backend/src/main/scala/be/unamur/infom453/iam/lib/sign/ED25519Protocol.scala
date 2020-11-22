package be.unamur.infom453.iam.lib.sign

import scala.concurrent.Future
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import java.security.{KeyFactory, PublicKey, Signature}

import org.apache.commons.codec.binary.Base64
import net.i2p.crypto.eddsa.EdDSASecurityProvider

import be.unamur.infom453.iam.lib._


case object ED25519Protocol extends SignProtocol {

  Security.addProvider(new EdDSASecurityProvider())
  val code = "ed25519"
  private val signature = Signature.getInstance("NONEwithEdDSA", "EdDSA");
  private val keyFactory = KeyFactory.getInstance("EdDSA", "EdDSA")

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
    val x509 = new X509EncodedKeySpec(Base64.decodeBase64(pub.replace("\n", "")))
    keyFactory.generatePublic(x509)
  }

}
