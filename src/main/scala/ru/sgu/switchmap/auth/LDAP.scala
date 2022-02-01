package ru.sgu.switchmap.auth

import com.unboundid.ldap.sdk.{LDAPConnection, SearchScope}
import ru.sgu.switchmap.config.LDAPConfig
import scala.util.Try
import zio._

trait LDAP {
  val conn: Task[LDAPConnection]
  def connect(username: String, password: String): Task[LDAPConnection]
  def findUser(username: String): Task[Boolean]
}

case class LDAPLive(cfg: LDAPConfig) extends LDAP {
  override val conn: Task[LDAPConnection] =
    connect(cfg.bindUser, cfg.bindPass)

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
    username: String
  ): Task[Boolean] = {
    conn.flatMap { conn =>
      val result = conn.search(
        cfg.baseDN,
        SearchScope.SUB,
        "(&(sAMAccountName=%s))".format(
          username
        )
      )

      if (result.getEntryCount > 0)
        ZIO.succeed(true)
      else
        ZIO.succeed(false)
    }
  }
}

object LDAPLive {
  val layer: RLayer[LDAPConfig, LDAP] = (LDAPLive(_)).toLayer
}

object LDAP {
  val conn: RIO[LDAP, LDAPConnection] = ZIO.serviceWithZIO[LDAP](_.conn)

  def connect(
    username: String,
    password: String
  ): RIO[LDAP, LDAPConnection] =
    ZIO.serviceWithZIO[LDAP](_.connect(username, password))

  def findUser(
    username: String
  ): RIO[LDAP, Boolean] =
    ZIO.serviceWithZIO[LDAP](_.findUser(username))
}
