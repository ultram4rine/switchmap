package services

import auth.User
import com.mohiva.play.silhouette.api.services.IdentityService

trait UserService extends IdentityService[User]
