package auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

trait UserService extends IdentityService[User] {
  def retrieveUserLoginInfo(
    username: String,
    providerID: String
  ): Future[Option[(User, LoginInfo)]]
}
