package modules

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto.{
  JcaCookieSigner,
  JcaCookieSignerSettings,
  JcaCrypter,
  JcaCrypterSettings
}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.TwitterProvider
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{
  CookieStateProvider,
  CookieStateSettings
}
import com.mohiva.play.silhouette.impl.providers.oauth2.{ LinkedInProvider, _ }
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import repositories.impl.{ OAuth2InfoDaoImpl, PasswordInfoDaoImpl, UserDaoImpl }
import repositories.{ OAuth2InfoDao, PasswordInfoDao, UserDao }
import services.UserService
import services.impl.UserServiceImpl
import utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv }

/**
	* Created by sromic on 05/05/2017.
	*
	* The Guice module which wires all Silhouette dependencies.
	*/
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
		* Configures the module.
		*/
  def configure() {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UserService].to[UserServiceImpl]
    bind[UserDao].to[UserDaoImpl]

    bind[PasswordInfoDao].to[PasswordInfoDaoImpl]
    bind[OAuth2InfoDao].to[OAuth2InfoDaoImpl]

    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDaoImpl]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDaoImpl]

    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]

    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
		* Provides the HTTP layer implementation.
		*
		* @param client Play's WS client.
		* @return The HTTP layer implementation.
		*/
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
		* Provides the Silhouette environment.
		*
		* @param userService The user service implementation.
		* @param authenticatorService The authentication service implementation.
		* @param eventBus The event bus instance.
		* @return The Silhouette environment.
		*/
  @Provides
  def provideEnvironment(userService: UserService,
                         authenticatorService: AuthenticatorService[CookieAuthenticator],
                         eventBus: EventBus): Environment[DefaultEnv] =
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )

  /**
		* Provides the social provider registry.
		*
		* @param linkedInProvider The LinkedIn provider implementation.
		* @return The Silhouette environment.
		*/
  @Provides
  def provideSocialProviderRegistry(linkedInProvider: LinkedInProvider): SocialProviderRegistry =
    SocialProviderRegistry(
      Seq(
        linkedInProvider
      )
    )

  /**
		* Provides the cookie signer for the OAuth2 state provider.
		*
		* @param configuration The Play configuration.
		* @return The cookie signer for the OAuth2 state provider.
		*/
  @Provides
  @Named("oauth2-state-cookie-signer")
  def provideOAuth2StageCookieSigner(configuration: Configuration): CookieSigner = {
    val config = configuration.underlying
      .as[JcaCookieSignerSettings]("silhouette.oauth2StateProvider.cookie.signer")

    new JcaCookieSigner(config)
  }

  /**
		* Provides the cookie signer for the authenticator.
		*
		* @param configuration The Play configuration.
		* @return The cookie signer for the authenticator.
		*/
  @Provides
  @Named("authenticator-cookie-signer")
  def provideAuthenticatorCookieSigner(configuration: Configuration): CookieSigner = {
    val config = configuration.underlying
      .as[JcaCookieSignerSettings]("silhouette.authenticator.cookie.signer")

    new JcaCookieSigner(config)
  }

  /**
		* Provides the crypter for the authenticator.
		*
		* @param configuration The Play configuration.
		* @return The crypter for the authenticator.
		*/
  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config =
      configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
		* Provides the auth info repository.
		*
		* @param passwordInfoDAO The implementation of the delegable password auth info DAO.
		* @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
		* @return The auth info repository instance.
		*/
  @Provides
  def provideAuthInfoRepository(
      passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
      oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]
  ): AuthInfoRepository =
    new DelegableAuthInfoRepository(passwordInfoDAO, oauth2InfoDAO)

  /**
		* Provides the authenticator service.
		*
		* @param cookieSigner The cookie signer implementation.
		* @param crypter The crypter implementation.
		* @param fingerprintGenerator The fingerprint generator implementation.
		* @param idGenerator The ID generator implementation.
		* @param configuration The Play configuration.
		* @param clock The clock instance.
		* @return The authenticator service.
		*/
  @Provides
  def provideAuthenticatorService(@Named("authenticator-cookie-signer") cookieSigner: CookieSigner,
                                  @Named("authenticator-crypter") crypter: Crypter,
                                  fingerprintGenerator: FingerprintGenerator,
                                  idGenerator: IDGenerator,
                                  configuration: Configuration,
                                  clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config =
      configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config,
                                   None,
                                   cookieSigner,
                                   encoder,
                                   fingerprintGenerator,
                                   idGenerator,
                                   clock)
  }

  /**
		* Provides the avatar service.
		*
		* @param httpLayer The HTTP layer implementation.
		* @return The avatar service implementation.
		*/
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
		* Provides the OAuth2 state provider.
		*
		* @param idGenerator The ID generator implementation.
		* @param cookieSigner The cookie signer implementation.
		* @param configuration The Play configuration.
		* @param clock The clock instance.
		* @return The OAuth2 state provider implementation.
		*/
  @Provides
  def provideOAuth2StateProvider(idGenerator: IDGenerator,
                                 @Named("oauth2-state-cookie-signer") cookieSigner: CookieSigner,
                                 configuration: Configuration,
                                 clock: Clock): OAuth2StateProvider = {

    val settings =
      configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
    new CookieStateProvider(settings, idGenerator, cookieSigner, clock)
  }

  /**
		* Provides the password hasher registry.
		*
		* @param passwordHasher The default password hasher implementation.
		* @return The password hasher registry.
		*/
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry =
    new PasswordHasherRegistry(passwordHasher)

  /**
		* Provides the credentials provider.
		*
		* @param authInfoRepository The auth info repository implementation.
		* @param passwordHasherRegistry The password hasher registry.
		* @return The credentials provider.
		*/
  @Provides
  def provideCredentialsProvider(
      authInfoRepository: AuthInfoRepository,
      passwordHasherRegistry: PasswordHasherRegistry
  ): CredentialsProvider =
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  /**
		* Provides the LinkedIn provider.
		*
		* @param httpLayer The HTTP layer implementation.
		* @param configuration The Play configuration.
		* @return The LinkedIn provider.
		*/
  @Provides
  def provideLinkedInProvider(httpLayer: HTTPLayer,
                              stateProvider: OAuth2StateProvider,
                              configuration: Configuration): LinkedInProvider =
    new LinkedInProvider(httpLayer,
                         stateProvider,
                         configuration.underlying.as[OAuth2Settings]("silhouette.linkedin"))
}
