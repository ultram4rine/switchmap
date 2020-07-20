package services

import java.util.UUID

import auth.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

trait UserService extends IdentityService[User] {
  def retrieveUserLoginInfo(
    id: UUID,
    providerID: String
  ): Future[Option[(User, LoginInfo)]]
}
