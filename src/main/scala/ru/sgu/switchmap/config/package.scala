package ru.sgu.switchmap

import com.comcast.ip4s.{Hostname, Port}
import org.http4s.Uri
import pureconfig.ConfigSource
import pureconfig.module.http4s._
import pureconfig.module.ip4s._
import zio.{Task, ULayer, URIO, ZEnvironment, ZLayer}

package object config {
  type Config = APIConfig with DBConfig with LDAPConfig with AppConfig

  final case class FullConfig(
    api: APIConfig,
    db: DBConfig,
    ldap: LDAPConfig,
    app: AppConfig
  )

  final case class AppConfig(
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

  val apiConfig: URIO[APIConfig, APIConfig] = URIO.service
  val dbConfig: URIO[DBConfig, DBConfig] = URIO.service
  val ldapConfig: URIO[LDAPConfig, LDAPConfig] = URIO.service
  val appConfig: URIO[AppConfig, AppConfig] = URIO.service

  object Config {
    import pureconfig.generic.auto._
    val live: ULayer[Config] = ZLayer.fromZIO(
      Task
        .attempt(ConfigSource.default.loadOrThrow[FullConfig])
        .map(c =>
          ZEnvironment(c.api) ++ ZEnvironment(c.db)
            ++ ZEnvironment(c.ldap) ++ ZEnvironment(c.app)
        )
        .orDie
    )
  }
}
