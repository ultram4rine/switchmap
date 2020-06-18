package auth

import play.api.libs.json.{Json, OFormat}

case class User(username: String)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
