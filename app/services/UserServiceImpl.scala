package services

import java.util.UUID

import auth.User
import auth.ldap.LDAP
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject() (ldap: LDAP)(implicit
  ec: ExecutionContext
) extends UserService {

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    userDAO.find(loginInfo)

  def retrieveUserLoginInfo(
    id: UUID,
    providerID: String
  ): Future[Option[(User, LoginInfo)]] = {
    loginInfoDAO.find(id, providerID)
  }

}
