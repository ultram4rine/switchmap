package auth.ldap

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{
  CommonSocialProfile,
  SocialProfileParser
}
import com.unboundid.ldap.sdk.SearchResultEntry

import scala.concurrent.Future

class LDAPProfileParser
    extends SocialProfileParser[
      SearchResultEntry,
      CommonSocialProfile,
      LDAPInfo
    ] {
  val ID = "LDAP"

  override def parse(
    searchEntry: SearchResultEntry,
    authInfo: LDAPInfo
  ): Future[CommonSocialProfile] =
    Future.successful {

      CommonSocialProfile(
        loginInfo = LoginInfo(ID, searchEntry.getAttributeValue("uid")),
        firstName = Some(searchEntry.getAttributeValue("givenName")),
        lastName = Some(searchEntry.getAttributeValue("sn")),
        fullName = Some(
          searchEntry.getAttributeValue("givenName") + " " + searchEntry
            .getAttributeValue("sn")
        ),
        email = Some(searchEntry.getAttributeValue("mail")),
        avatarURL = None
      )
    }
}
