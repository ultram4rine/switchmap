package auth

import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(implicit ec: ExecutionContext) extends UserService {
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    Future { Some(User("admin")) }

  def retrieveUserLoginInfo(
    username: String,
    providerID: String
  ): Future[Option[(User, LoginInfo)]] =
    Future {
      Some(User("admin"), LoginInfo("LDAP", "admin"))
    }
}
