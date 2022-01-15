package ru.sgu.switchmap

import com.comcast.ip4s.{Hostname, Port}
import org.http4s.Uri
import pureconfig.ConfigSource
import pureconfig.module.http4s._
import pureconfig.module.ip4s._
import zio.{Has, Task, ULayer, URIO, ZIO, ZLayer}

package object config {
  type Config = Has[APIConfig]
    with Has[DBConfig]
    with Has[LDAPConfig]
    with Has[AppConfig]

  final case class FullConfig(
    api: APIConfig,
    db: DBConfig,
    ldap: LDAPConfig,
    app: AppConfig
  )

  final case class AppConfig(
    servers: List[OpenAPIServer],
    jwtKey: String,
    netdataHost: String,
    netdataPort: Int,
    seensHost: Uri,
    snmpCommunities: List[String],
    dnsSuffix: String
  )

  final case class APIConfig(endpoint: Hostname, port: Port)
  final case class DBConfig(
    url: String,
    user: String,
    password: String
  )
  final case class LDAPConfig(
    host: String,
    port: Int,
    domain: String,
    bindUser: String,
    bindPass: String,
    baseDN: String
  )

  val apiConfig: URIO[Has[APIConfig], APIConfig] = ZIO.access(_.get)
  val dbConfig: URIO[Has[DBConfig], DBConfig] = ZIO.access(_.get)
  val ldapConfig: URIO[Has[LDAPConfig], LDAPConfig] = ZIO.access(_.get)
  val appConfig: URIO[Has[AppConfig], AppConfig] = ZIO.access(_.get)

  object Config {
    import pureconfig.generic.auto._
    val live: ULayer[Config] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[FullConfig])
        .map(c => Has(c.api) ++ Has(c.db) ++ Has(c.ldap) ++ Has(c.app))
        .orDie
    )
  }

  final case class OpenAPIServer(
    url: String,
    description: Option[String]
  )
}
