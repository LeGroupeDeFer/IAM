/*
  Big up to the guy that helped us create this file : gregparsons

  Resources that have really be useful
    - https://github.com/gregparsons/Scala-Rsa-Signature
    - https://raymii.org/s/tutorials/Sign_and_verify_text_files_to_public_keys_via_the_OpenSSL_Command_Line.html

*/

package be.unamur.infom453.iam.lib

import org.apache.commons.codec.binary.Base64

import java.security.{KeyFactory, PrivateKey, PublicKey, Signature}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import scala.concurrent.Future

object RSASign {
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

/*

Digital signature ensures:
--authentication
--non-repudiation
--message integrity

Ref: https://en.wikipedia.org/wiki/Digital_signature


Build the key files

Take a string, hash it. Then digitally sign that hash with your private key. Send
it to someone who has your public key. They run verify() using your public key.
Assuming they know (or you also give them the thing you're verifying) then if
verify returns true, they know that the private key they have correspond to the
public key used to make the signature. If the public key was obtained in a verifiable
way from the person with the private key, the message is authentic--from whom
it was said to be from.

# hash (not really important)
echo "mythingname" | openssl sha1 > name_sha1.txt

# generate rsa key pair

# private key
openssl genrsa -out rsa4096_private.pem 4096
openssl pkcs8 -topk8 -inform PEM -outform DER -in rsa4096_private.pem -out rsa4096_private.der -nocrypt

# public key
openssl rsa -in rsa4096_private.pem -pubout
openssl rsa -in rsa4096_private.pem -pubout -outform DER -out rsa4096_public.der

# Sign / verify
# https://www.openssl.org/docs/manmaster/apps/rsautl.html
#
#sign (from stdin, use ctrl-d to end)

# Test sign and verify from the command line using the generated keys.
# Sign
openssl rsautl -sign -inkey rsa4096_private.pem -out sigfile.rsa

# Verify (presents string originally from stdin)
openssl rsautl -verify -in sigfile.rsa -inkey rsa4096_public.pem -pubin

# if openssl sha1 > name_sha1.txt == "mythingname", then the
# private key used to sign the hash of this name is authenticated

# all in one line
echo "myvehiclename" | openssl sha1 | openssl rsautl -sign -inkey rsa4096_private.pem | openssl rsautl -verify -inkey rsa4096_public.pem -pubin; echo "myvehiclename" | openssl sha1


########
# now do this all in scala
#######

#http://stackoverflow.com/a/19387517/3680466
#Convert private Key to PKCS#8 format (so Java can read it)
openssl pkcs8 -topk8 -inform PEM -outform DER -in rsa4096_private.pem -out rsa4096_private.der -nocrypt

#http://stackoverflow.com/a/19387517/3680466
#Output public key portion in DER format (so Java can read it)


References:
https://gist.github.com/urcadox/6173812
https://docs.oracle.com/javase/7/docs/api/index.html?javax/crypto/Cipher.html
http://stackoverflow.com/a/19387517/3680466
http://www.programcreek.com/java-api-examples/java.security.Signature
http://codeartisan.blogspot.com/2009/05/public-key-cryptography-in-java.html
http://developer.android.com/reference/javax/crypto/package-summary.html
http://www.logikdev.com/tag/javax-crypto/
http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#HowSSLWorks
http://stackoverflow.com/questions/5140425/openssl-command-line-to-verify-the-signature/5141195#5141195
https://www.openssl.org/docs/manmaster/apps/rsautl.html
http://connect2id.com/products/nimbus-jose-jwt/openssl-key-generation
https://www.madboa.com/geek/openssl/#how-do-i-create-an-md5-or-sha1-digest-of-a-file
https://commons.apache.org/proper/commons-codec/archives/1.10/apidocs/index.html
*/