package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{
  Crypter,
  CrypterAuthenticatorEncoder,
  Signer
}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{
  Environment,
  EventBus,
  Silhouette,
  SilhouetteProvider
}
import com.mohiva.play.silhouette.crypto.{
  JcaCrypter,
  JcaCrypterSettings,
  JcaSigner,
  JcaSignerSettings
}
import com.mohiva.play.silhouette.impl.authenticators.{
  JWTAuthenticator,
  JWTAuthenticatorService,
  JWTAuthenticatorSettings
}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.providers.state.{
  CsrfStateItemHandler,
  CsrfStateSettings
}
import com.mohiva.play.silhouette.impl.util.{
  DefaultFingerprintGenerator,
  PlayCacheLayer,
  SecureRandomIDGenerator
}
import com.mohiva.play.silhouette.password.{
  BCryptPasswordHasher,
  BCryptSha256PasswordHasher
}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.Cookie
import services.{UserService, UserServiceImpl}
import utils.auth.DefaultEnv
import utils.auth.ldap.{LDAP, LDAPProvider}

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule {

  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UserService].to[UserServiceImpl]
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
  @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings](
      "silhouette.csrfStateItemHandler.signer"
    )

    new JcaSigner(config)
  }

  @Provides
  @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config =
      configuration.underlying.as[JcaSignerSettings](
        "silhouette.authenticator.signer"
      )

    new JcaSigner(config)
  }

  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config =
      configuration.underlying.as[JcaCrypterSettings](
        "silhouette.authenticator.crypter"
      )

    new JcaCrypter(config)
  }

  @Provides
  def provideAuthInfoRepository(): AuthInfoRepository = {
    new DelegableAuthInfoRepository()
  }

  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-crypter") crypter: Crypter,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock
  ): AuthenticatorService[JWTAuthenticator] = {
    val config =
      configuration.underlying.as[JWTAuthenticatorSettings](
        "silhouette.authenticator"
      )
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  }

  @Provides
  def provideCsrfStateItemHandler(
    idGenerator: IDGenerator,
    @Named("csrf-state-item-signer") signer: Signer,
    configuration: Configuration
  ): CsrfStateItemHandler = {
    val settings =
      configuration.underlying.as[CsrfStateSettings](
        "silhouette.csrfStateItemHandler"
      )
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(
      new BCryptSha256PasswordHasher(),
      Seq(new BCryptPasswordHasher())
    )
  }

  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry
  ): CredentialsProvider = {
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  @Provides
  def provideLDAPProvider(ldap: LDAP): LDAPProvider = {
    new LDAPProvider(ldap)
  }

}
