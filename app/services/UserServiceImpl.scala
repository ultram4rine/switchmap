package services

import auth.User
import auth.ldap.LDAP
import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(ldap: LDAP)(implicit ec: ExecutionContext)
    extends UserService {
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    ldap.getUserDN(loginInfo.providerID).map {
      case Some(dn) => Some(User(dn))
      case None     => None
    }
  }
}
