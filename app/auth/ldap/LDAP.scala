package auth.ldap

import java.security.MessageDigest

import com.unboundid.ldap.sdk._
import com.unboundid.util.ssl.{
  SSLUtil,
  TrustAllTrustManager,
  TrustStoreTrustManager
}
import javax.inject.{Inject, Singleton}
import javax.net.ssl.X509TrustManager
import play.api.Configuration
import play.api.cache._

import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LDAP @Inject() (implicit
  ec: ExecutionContext,
  configuration: Configuration,
  cache: AsyncCacheApi
) {

  private val protocol =
    configuration.getOptional[String]("ldap.protocol").getOrElse("ldap")
  private val useKeystore =
    configuration.getOptional[Boolean]("ldap.useKeystore").getOrElse(false)

  private val host1 = configuration.get[String]("ldap.host1")
  private val host2 =
    configuration.getOptional[String]("ldap.host2").getOrElse("")
  private val host3 =
    configuration.getOptional[String]("ldap.host3").getOrElse("")

  private val port1 =
    configuration.getOptional[Int]("ldap.port1").getOrElse(389)
  private val port2 =
    configuration.getOptional[Int]("ldap.port2").getOrElse(389)
  private val port3 =
    configuration.getOptional[Int]("ldap.port3").getOrElse(389)

  private val bindDN = configuration.get[String]("ldap.bindDN")
  private val bindPass = configuration.get[String]("ldap.bindPass")
  private val baseDN = configuration.get[String]("ldap.baseDN")
  private val uidAttribute = configuration.get[String]("ldap.uidAttribute")

  private val poolSize =
    configuration.getOptional[Int]("ldap.poolSize").getOrElse(3)

  private val trustStore = configuration.get[String]("ldap.trustStore")
  private val trustStorePass =
    configuration.get[String]("ldap.trustStorePass").toCharArray
  private val trustStoreType = configuration.get[String]("ldap.trustStoreType")

  private val cacheDuration = Duration(
    configuration.getOptional[Int]("ldap.cacheDuration").getOrElse(600),
    MILLISECONDS
  )

  private val serverAddresses = Array(host1, host2, host3)
  private val serverPorts = Array(port1, port2, port3)

  private val trustManager: X509TrustManager with Serializable = {
    (protocol, useKeystore) match {
      case ("ldaps", true) =>
        new TrustStoreTrustManager(
          trustStore,
          trustStorePass,
          trustStoreType,
          true
        )
      case ("ldaps", false) =>
        new TrustAllTrustManager()
      case _ =>
        null
    }
  }

  private val connectionPool: LDAPConnectionPool = protocol match {
    case "ldaps" =>
      new LDAPConnectionPool(
        new FailoverServerSet(
          serverAddresses,
          serverPorts,
          new SSLUtil(trustManager).createSSLSocketFactory()
        ),
        new SimpleBindRequest(bindDN, bindPass),
        poolSize
      )
    case "ldap" =>
      new LDAPConnectionPool(
        new FailoverServerSet(serverAddresses, serverPorts),
        new SimpleBindRequest(bindDN, bindPass),
        poolSize
      )
    case _ =>
      null
  }

  def getDN(
    searchEntries: java.util.List[com.unboundid.ldap.sdk.SearchResultEntry]
  ): Future[Option[String]] = {
    searchEntries.size match {
      case 0 => Future { None }
      case _ => Future { Some(searchEntries.get(0).getDN) }
    }
  }

  def getUserDN(uid: String): Future[Option[String]] = {
    val cacheKey = "userDN." + uid
    cache.getOrElseUpdate[Option[String]](cacheKey) {
      println("LDAP: get DN for " + uid)
      val searchEntries
        : java.util.List[com.unboundid.ldap.sdk.SearchResultEntry] =
        connectionPool
          .search(
            new SearchRequest(
              baseDN,
              SearchScope.SUB,
              Filter.createEqualityFilter(uidAttribute, uid)
            )
          )
          .getSearchEntries
      getDN(searchEntries)
    }
  }

  def bind(uid: String, pass: String): Future[Int] = {
    val msg: String = uid + pass + "ela_salt_201406"
    val hash: String = MessageDigest
      .getInstance("SHA-256")
      .digest(msg.getBytes)
      .foldLeft("")((s: String, b: Byte) =>
        s + Character.forDigit((b & 0xf0) >> 4, 16) + Character
          .forDigit(b & 0x0f, 16)
      )
    val cacheKey = "bindResult." + hash
    cache.getOrElseUpdate[Int](cacheKey, cacheDuration) {
      getUserDN(uid).flatMap {
        case Some(dn) =>
          Future {
            println("LDAP: binding " + uid + " hash=" + hash)
            connectionPool
              .bindAndRevertAuthentication(new SimpleBindRequest(dn, pass))
              .getResultCode
              .intValue()
          }
        case _ => Future { 1 }
      }
    }
  }

}
