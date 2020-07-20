package services

import utils.auth.User
import com.mohiva.play.silhouette.api.services.IdentityService

trait UserService extends IdentityService[User]
