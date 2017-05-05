package controllers

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import _root_.services.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import models.User
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.Controller
import utils.auth.DefaultEnv

import scala.concurrent.{ ExecutionContext, Future }

/**
	* Created by sromic on 05/05/2017.
	*
	* The sign up controller.
	*
	* @param messagesApi The Play messages API.
	* @param silhouette The Silhouette environment.
	* @param userService The user service implementation.
	* @param authInfoRepository The auth info repository implementation.
	* @param avatarService The avatar service implementation.
	*/
@Singleton
class SignUpController @Inject()(
    val messagesApi: MessagesApi,
    silhouette: Silhouette[DefaultEnv],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    avatarService: AvatarService,
    passwordHasherRegistry: PasswordHasherRegistry
)(implicit val ec: ExecutionContext, val webJarAssets: WebJarAssets)
    extends Controller
    with I18nSupport {

  /**
		* Views the `Sign Up` page.
		*
		* @return The result to display.
		*/
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  /**
		* Handles the submitted form.
		*
		* @return The result to display.
		*/
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        val result    = Redirect(routes.SignUpController.view())
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(_) =>
            Future.successful(result.flashing("error" -> Messages("user.exists")))
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              userID = UUID.randomUUID(),
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              fullName = Some(data.firstName + " " + data.lastName),
              email = Some(data.email),
              avatarURL = None
            )
            for {
              avatar   <- avatarService.retrieveURL(data.email)
              user     <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
            } yield {
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}
