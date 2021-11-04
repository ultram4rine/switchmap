package ru.sgu.switchmap.auth

import zio._
import com.unboundid.ldap.sdk.{LDAPConnection, SearchScope}

import ru.sgu.switchmap.config.LDAPConfig

import scala.util.Try

trait LDAP {
  val conn: Task[LDAPConnection]
  def connect(username: String, password: String): Task[LDAPConnection]
  def findUser(username: String): Task[Boolean]
}

case class LDAPLive(cfg: LDAPConfig) extends LDAP {
  override val conn: Task[LDAPConnection] =
    this.connect(cfg.bindUser, cfg.bindPass)

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
    this.conn.flatMap { conn =>
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
  val layer: RLayer[Has[LDAPConfig], Has[LDAP]] = (LDAPLive(_)).toLayer
}

object LDAP {
  def connect(
    username: String,
    password: String
  ): RIO[Has[LDAP], LDAPConnection] =
    ZIO.serviceWith[LDAP](_.connect(username, password))

  def findUser(
    username: String
  ): RIO[Has[LDAP], Boolean] =
    ZIO.serviceWith[LDAP](_.findUser(username))
}
