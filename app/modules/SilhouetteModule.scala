package modules

import auth.ldap.{LDAPProvider, LDAPSettings}
import auth.{DefaultEnv, UserService}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{
  Crypter,
  CrypterAuthenticatorEncoder
}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{
  Environment,
  EventBus,
  Silhouette,
  SilhouetteProvider
}
import com.mohiva.play.silhouette.impl.authenticators.{
  JWTAuthenticator,
  JWTAuthenticatorService,
  JWTAuthenticatorSettings
}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import com.mohiva.play.silhouette.impl.util.{
  DefaultFingerprintGenerator,
  PlayCacheLayer,
  SecureRandomIDGenerator
}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class SilhouetteModule(implicit ec: ExecutionContext)
    extends AbstractModule
    with ScalaModule {
  override def configure() {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(
      new DefaultFingerprintGenerator(false)
    )
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[JWTAuthenticator],
    eventBus: EventBus
  ): Environment[DefaultEnv] = {
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  @Provides
  def provideSocialProviderRegistry(
    ldapProvider: LDAPProvider
  ): SocialProviderRegistry = {
    SocialProviderRegistry(Seq(ldapProvider))
  }

  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-crypter") crypter: Crypter,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock
  ): AuthenticatorService[JWTAuthenticator] = {
    val config =
      configuration.underlying.getEnum[JWTAuthenticatorSettings](
        Class[JWTAuthenticatorSettings],
        "silhouette.authenticator"
      )
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  }

  @Provides
  def provideLDAPProvider(
    httpLayer: HTTPLayer,
    configuration: Configuration
  ): LDAPProvider = {

    new LDAPProvider(
      httpLayer,
      configuration.underlying
        .getEnum[LDAPSettings](Class[LDAPSettings], "silhouette.ldap")
    )
  }
}
