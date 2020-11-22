package be.unamur.infom453.iam.lib.sign

import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyFactory, KeyPairGenerator, PublicKey, Signature}

import scala.concurrent.Future
import org.apache.commons.codec.binary.Base64
import be.unamur.infom453.iam.lib._


case object RSAProtocol extends SignProtocol {

  val code = "rsa"

  private[iam] val signature  = Signature.getInstance("SHA512withRSA")
  private[iam] val keyFactory = KeyFactory.getInstance("RSA")
  private[iam] val generator  = KeyPairGenerator.getInstance("RSA")

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
