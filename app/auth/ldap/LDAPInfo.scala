package auth.ldap

import com.mohiva.play.silhouette.api.AuthInfo

case class LDAPInfo(
  code: String,
  username: String,
  password: String
) extends AuthInfo
