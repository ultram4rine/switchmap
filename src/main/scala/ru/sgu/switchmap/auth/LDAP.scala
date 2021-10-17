package ru.sgu.switchmap.auth

import zio._
import zio.console._
import com.unboundid.ldap.sdk._

import ru.sgu.switchmap.config.LDAPConfig

trait LDAP {
  val conn: LDAPConnection
  def findUser(username: String): UIO[Boolean]
}

case class LDAPLive(cfg: LDAPConfig) extends LDAP {
  override val conn: LDAPConnection = new LDAPConnection(
    cfg.host,
    cfg.port,
    "%s@%s".format(cfg.bindUser, cfg.domain),
    cfg.bindPass
  )
  override def findUser(username: String): UIO[Boolean] = {
    val result = this.conn.search(
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
  def findUser(username: String): URIO[Has[LDAP], Boolean] =
    ZIO.serviceWith[LDAP](_.findUser(username))
}
