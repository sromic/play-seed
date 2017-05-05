package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{ LoginEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import forms.SignInForm
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.Controller
import services.UserService
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

/**
	* Created by sromic on 05/05/2017.
	*/
@Singleton
class SignInController @Inject()(
    val messagesApi: MessagesApi,
    silhouette: Silhouette[DefaultEnv],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    socialProviderRegistry: SocialProviderRegistry,
    configuration: Configuration
)(implicit val webJarAssets: WebJarAssets, val ec: ExecutionContext)
    extends Controller
    with I18nSupport {

  /**
		* Views the `Sign In` page.
		*
		* @return The result to display.
		*/
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signIn(SignInForm.form, socialProviderRegistry)))
  }

  /**
		* Handles the submitted form.
		*
		* @return The result to display.
		*/
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider
          .authenticate(credentials)
          .flatMap {
            loginInfo =>
              val result = Redirect(routes.ApplicationController.index())
              userService.retrieve(loginInfo).flatMap {
                case Some(user) =>
                  val c = configuration.underlying
                  silhouette.env.authenticatorService
                    .create(loginInfo)
                    .map {
                      case authenticator => authenticator
                    }
                    .flatMap { authenticator =>
                      silhouette.env.eventBus.publish(LoginEvent(user, request))
                      silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                        silhouette.env.authenticatorService.embed(v, result)
                      }
                    }
                case None =>
                  Future.failed(new IdentityNotFoundException("Couldn't find user"))
              }
          }
          .recover {
            case e: ProviderException =>
              Redirect(routes.SignInController.view())
                .flashing("error" -> Messages("invalid.credentials"))
          }
      }
    )
  }
}
