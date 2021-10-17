package ru.sgu.switchmap.auth

import zio._
import zio.console._
import com.unboundid.ldap.sdk._

import ru.sgu.switchmap.config.LDAPConfig

object ldap {
  type LDAP = Has[LDAP.Service]

  object LDAP {
    trait Service {
      val conn: LDAPConnection
      def findUser(username: String): Boolean
    }

    val live: ZLayer[Has[LDAPConfig], Throwable, LDAP] = ZLayer.fromService {
      cfg =>
        new Service {
          val conn: LDAPConnection =
            new LDAPConnection(
              cfg.host,
              cfg.port,
              "%s@%s".format(cfg.bindUser, cfg.domain),
              cfg.bindPass
            )
          def findUser(username: String): Boolean = {
            val result = this.conn.search(
              cfg.baseDN,
              SearchScope.SUB,
              "(&(sAMAccountName=%s)(memberOf=%s))".format(
                username,
                cfg.baseDN
              )
            )

            if (result.getEntryCount > 0)
              true
            else
              false
          }
        }
    }
  }
}
