package utils.auth

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json.{Json, OFormat}

case class User(username: String) extends Identity

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
