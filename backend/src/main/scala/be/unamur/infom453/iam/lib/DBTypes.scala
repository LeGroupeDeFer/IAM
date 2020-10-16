package be.unamur.infom453.iam.lib

import org.mindrot.jbcrypt.BCrypt
import be.unamur.infom453.iam.Implicits.api


object DBTypes {

  import api._

  object Hash {
    def of(pw: String) = Hash(BCrypt.hashpw(pw, BCrypt.gensalt()))
  }

  case class Hash(value: String) extends MappedTo[String] {
    def check(password: String): Boolean = BCrypt.checkpw(password, value)
  }

}
