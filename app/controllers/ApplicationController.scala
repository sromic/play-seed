package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ApplicationController @Inject()(
    val messagesApi: MessagesApi,
    silhouette: Silhouette[DefaultEnv],
    socialProviderRegistry: SocialProviderRegistry
)(implicit val webJarAssets: WebJarAssets, val ec: ExecutionContext)
    extends Controller
    with I18nSupport {

  /**
    * Handles the index action.
    *
    * @return The result to display.
    */
  def index = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
