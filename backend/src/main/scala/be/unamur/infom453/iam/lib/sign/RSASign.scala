package be.unamur.infom453.iam.lib.sign

import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyFactory, PublicKey, Signature}

import be.unamur.infom453.iam.lib._
import org.apache.commons.codec.binary.Base64

import scala.concurrent.Future

case object RSASign {

  def verify(publicKey: PublicKey, signedCipherTestEncoded: String, plainText: String): Future[Boolean] = Future {
    val signer = Signature.getInstance("SHA512withRSA")
    signer.initVerify(publicKey)
    signer.update(plainText.getBytes("UTF-8"))
    System.out.println(plainText)

    val signedCipherTest = Base64.decodeBase64(signedCipherTestEncoded)

    if (!signer.verify(signedCipherTest)) {
      throw new Exception("purple u lookin sus!")
    } else {
      true
    }
  }

  def publicKeyFromString(pub: String): PublicKey = {
    val x509 = new X509EncodedKeySpec(Base64.decodeBase64(pub.replace("\n", "")))
    KeyFactory.getInstance("RSA").generatePublic(x509)
  }

  def publicKeyFromBytes(bytes: Array[Byte]): PublicKey = {
    KeyFactory.getInstance("RSA").generatePublic(new PKCS8EncodedKeySpec(bytes))
  }
}
