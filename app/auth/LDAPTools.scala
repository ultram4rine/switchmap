package auth

import com.unboundid.ldap.sdk._
import ru.sgu.switchmap.configuration.{Configuration, LDAP}

import scala.concurrent.{ExecutionContext, Future}

object LDAPTools {
  private val ldap: LDAP = Configuration.ldap

  def bind(userName: String, password: String): LDAPConnection = {
    new LDAPConnection(
      ldap.host,
      ldap.port,
      "%s@%s".format(userName, ldap.domain),
      password
    )
  }

  def adminBind: LDAPConnection =
    bind(ldap.bindDN, ldap.bindPass)

  private def search(
    conn: LDAPInterface,
    username: String
  ): Option[SearchResultEntry] = {
    val result = conn.search(
      ldap.baseDN,
      SearchScope.SUB,
      "(%s=%s)".format(ldap.uidAttribute, username)
    )
    if (result.getEntryCount > 0)
      Some(result.getSearchEntries.get(0))
    else
      None
  }

  def authenticate(username: String, password: String): Option[User] = {
    bind(username, password)
      .flatMap { conn =>
        val r = findUser(conn, username)
        r.onComplete { _ => conn.close() }
        r
      }
      .recover {
        case e: LDAPException if e.getResultCode.intValue() == 49 => None
      }
  }

  private def findUser(conn: LDAPInterface, username: String): Option[User] =
    search(conn, username).map(_.map(makeUser(_)))

  private def makeUser(entry: Entry): User = {
    val uid = entry.getAttributeValue(ldap.uidAttribute)
    User(uid)
  }
}
