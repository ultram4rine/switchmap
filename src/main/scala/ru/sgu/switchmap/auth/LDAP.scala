package ru.sgu.switchmap.auth

import zio._
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPException, SearchScope}

import ru.sgu.switchmap.config.LDAPConfig

import scala.util.Try

trait LDAP {
  def connect(username: String, password: String): Task[LDAPConnection]
  def findUser(conn: LDAPConnection, username: String): UIO[Boolean]
}

case class LDAPLive(cfg: LDAPConfig) extends LDAP {
  override def connect(
    username: String,
    password: String
  ): Task[LDAPConnection] =
    Task.fromTry(
      Try {
        new LDAPConnection(
          cfg.host,
          cfg.port,
          "%s@%s".format(username, cfg.domain),
          password
        )
      }
    )

  override def findUser(
    conn: LDAPConnection,
    username: String
  ): UIO[Boolean] = {
    val result = conn.search(
      cfg.baseDN,
      SearchScope.SUB,
      "(&(sAMAccountName=%s)(memberOf=%s))".format(
        username,
        cfg.baseDN
      )
    )

    if (result.getEntryCount > 0)
      ZIO.succeed(true)
    else
      ZIO.succeed(false)
  }
}

object LDAPLive {
  val layer: RLayer[Has[LDAPConfig], Has[LDAP]] = (LDAPLive(_)).toLayer
}

object LDAP {
  def connect(
    username: String,
    password: String
  ): RIO[Has[LDAP], LDAPConnection] =
    ZIO.serviceWith[LDAP](_.connect(username, password))

  def findUser(
    conn: LDAPConnection,
    username: String
  ): RIO[Has[LDAP], Boolean] =
    ZIO.serviceWith[LDAP](_.findUser(conn, username))
}
