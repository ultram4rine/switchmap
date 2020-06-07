package ru.sgu.switchmap.configuration

import com.typesafe.config.{Config, ConfigFactory}

object Configuration {
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
  poolSize: Int,
  cacheDuration: Int
)
