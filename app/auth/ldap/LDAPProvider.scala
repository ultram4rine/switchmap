package auth.ldap

import com.mohiva.play.silhouette.api.util.{ExtractableRequest, HTTPLayer}
import com.mohiva.play.silhouette.impl.exceptions.{
  AccessDeniedException,
  ProfileRetrievalException,
  UnexpectedResponseException
}
import com.mohiva.play.silhouette.impl.providers.{
  CommonSocialProfileBuilder,
  SocialProvider
}
import com.unboundid.ldap.sdk.{
  LDAPConnection,
  SearchResult,
  SearchResultEntry,
  SearchScope
}
import com.unboundid.util.ssl.{
  JVMDefaultTrustManager,
  SSLUtil,
  TrustAllTrustManager
}
import javax.net.ssl.SSLSocketFactory
import play.api.mvc.{Result, Results}

import scala.concurrent.Future

class LDAPProvider(
  protected val httpLayer: HTTPLayer,
  val settings: LDAPSettings
) extends SocialProvider
    with CommonSocialProfileBuilder {

  override type A = LDAPInfo

  override type Settings = LDAPSettings

  override type Content = SearchResultEntry

  override val id = "LDAP"

  type Self = LDAPProvider

  val profileParser = new LDAPProfileParser

  def withSettings(f: Settings => Settings): LDAPProvider = {
    new LDAPProvider(httpLayer, f(settings))
  }

  override protected val urls = Map("hostname" -> settings.hostname)

  val SpecifiedProfileError =
    "[Silhouette][%s] Error retrieving profile information. Error code: %s, message: %s"
  val AuthorizationError =
    "[Silhouette][%s] Authorization server returned error: %s"

  override protected def buildProfile(authInfo: LDAPInfo): Future[Profile] = {
    val username = authInfo.username
    val password = authInfo.password
    val baseUserNamespace = settings.userDN + "," + settings.baseDN
    val baseGroupNamespace = settings.groupDN + "," + settings.baseDN

    val trustManager =
      if (settings.trustAllCertificates) new TrustAllTrustManager()
      else JVMDefaultTrustManager.getInstance()
    val sslUtil: SSLUtil = new SSLUtil(trustManager)
    val socketFactory: SSLSocketFactory = sslUtil.createSSLSocketFactory()
    var ldapConnection: LDAPConnection = null
    try {
      ldapConnection =
        new LDAPConnection(socketFactory, settings.hostname, settings.port)

      val dn = "uid=" + username + "," + baseUserNamespace
      ldapConnection.bind(dn, password)
      val searchFilter
        : com.unboundid.ldap.sdk.Filter = com.unboundid.ldap.sdk.Filter.create(
        "(&(objectClass=" +
          settings.objectClass + ")(memberOf=cn=" + settings.group + "," + baseGroupNamespace + ")(uid=" + username + "))"
      )

      val searchResult: SearchResult =
        ldapConnection.search(settings.baseDN, SearchScope.SUB, searchFilter)

      if (searchResult.getEntryCount == 1) {
        val searchEntry = searchResult.getSearchEntry(dn)
        profileParser.parse(searchEntry, authInfo)
      } else {
        throw new ProfileRetrievalException(
          SpecifiedProfileError.format(id, 403, "user not in the group")
        )
      }
    } catch {
      case e: Exception =>
        throw new ProfileRetrievalException(
          SpecifiedProfileError.format(id, 403, e.getMessage)
        )
    } finally {
      if (ldapConnection != null)
        ldapConnection.close()
    }
  }

  def authenticate[B]()(implicit
    request: ExtractableRequest[B]
  ): Future[Either[Result, LDAPInfo]] = {
    request.extractString("error").map {
      // TODO: may remove this part is not used.
      // refer to https://github.com/mohiva/play-silhouette/blob/master/silhouette/app/com/mohiva/play/silhouette/impl/providers/OAuth2Provider.scala
      case e @ "access_denied" =>
        new AccessDeniedException(AuthorizationError.format(id, e))
      case e =>
        new UnexpectedResponseException(AuthorizationError.format(id, e))
    } match {
      case Some(throwable) => Future.failed(throwable)
      case None =>
        request.extractString("code") match {
          case Some(_) =>
            val authInfo = LDAPInfo(
              code = request.extractString("code").getOrElse(""),
              username = request.extractString("username").getOrElse(""),
              password = request.extractString("password").getOrElse("")
            )
            Future.successful(Right(authInfo))
          case None =>
            Future.successful(Left(Results.Redirect("ldapinput")))
        }
    }
  }
}
