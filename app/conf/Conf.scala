package conf

import com.typesafe.config.{Config, ConfigFactory}

object Conf {
  val conf: Config = ConfigFactory.load()
  val jwtKey: String = conf.getString("jwtKey")
  val ldap: LDAP = conf.getEnum(Class[LDAP], "ldap")
}

case class LDAP(
  protocol: String,
  host: String,
  port: Int,
  bindDN: String,
  bindPass: String,
  baseDN: String,
  uidAttribute: String,
  domain: String,
  poolSize: Int,
  cacheDuration: Int
)
