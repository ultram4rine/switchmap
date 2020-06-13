package auth.ldap

case class LDAPSettings(
  hostname: String,
  group: String,
  port: Int,
  baseDN: String,
  userDN: String,
  groupDN: String,
  objectClass: String,
  trustAllCertificates: Boolean
)
