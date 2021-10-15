package ru.sgu.switchmap

import pureconfig.ConfigSource
import zio._

package object config {
  type Config = Has[APIConfig]
    with Has[DBConfig]
    with Has[LDAPConfig]
    with Has[String]

  final case class AppConfig(
    api: APIConfig,
    db: DBConfig,
    ldap: LDAPConfig,
    dnsSuffix: String
  )

  final case class APIConfig(endpoint: String, port: Int)
  final case class DBConfig(
    driver: String,
    url: String,
    user: String,
    password: String
  )
  final case class LDAPConfig(
    url: String,
    baseDN: String,
    bindUser: String,
    bindPass: String
  )

  val apiConfig: URIO[Has[APIConfig], APIConfig] = ZIO.access(_.get)
  val dbConfig: URIO[Has[DBConfig], DBConfig] = ZIO.access(_.get)
  val ldapConfig: URIO[Has[LDAPConfig], LDAPConfig] = ZIO.access(_.get)
  val dnsSuffix: URIO[Has[String], String] = ZIO.access(_.get)

  object Config {
    import pureconfig.generic.auto._
    val live: ULayer[Config] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.api) ++ Has(c.db) ++ Has(c.ldap) ++ Has(c.dnsSuffix))
        .orDie
    )
  }
}
