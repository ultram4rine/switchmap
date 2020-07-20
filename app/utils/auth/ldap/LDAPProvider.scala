package utils.auth.ldap

import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginInfo, Provider}
import com.mohiva.play.silhouette.impl.exceptions.InvalidPasswordException

import scala.concurrent.{ExecutionContext, Future}

class LDAPProvider(ldap: LDAP) extends Provider {
  override def id: String = "LDAP"

  def authenticate(
    credentials: Credentials
  )(implicit ec: ExecutionContext): Future[LoginInfo] = {
    ldap.bind(credentials.identifier, credentials.password).map {
      case 0 => LoginInfo(id, credentials.identifier)
      case _ =>
        throw new InvalidPasswordException(
          s"invalid password for user ${credentials.identifier}"
        )
    }
  }
}
