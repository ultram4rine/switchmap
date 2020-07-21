package controllers

import utils.auth.DefaultEnv
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.{Logger, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Request}
import services._

import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject() (
  authenticateService: AuthenticateService,
  silhouette: Silhouette[DefaultEnv],
  configuration: Configuration,
  clock: Clock
)(implicit ex: ExecutionContext)
    extends AbstractAuthController(silhouette, configuration, clock)
    with Logger {

  def submit: Action[AnyContent] =
    silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
      SignInForm.form
        .bindFromRequest()
        .fold(
          _ => Future.successful(BadRequest),
          data => {
            authenticateService
              .credentials(data.username, data.password)
              .flatMap {
                case Success(user) =>
                  val loginInfo =
                    LoginInfo(CredentialsProvider.ID, user.username)
                  authenticateUser(user, loginInfo, data.rememberMe)
                case InvalidPassword(attemptsAllowed) =>
                  Future.successful(
                    Forbidden(
                      Json.obj(
                        "errorCode" -> "InvalidPassword",
                        "attemptsAllowed" -> attemptsAllowed
                      )
                    )
                  )
                case UserNotFound =>
                  Future.successful(
                    Forbidden(Json.obj("errorCode" -> "UserNotFound"))
                  )
                case ToManyAuthenticateRequests(nextAllowedAttemptTime) =>
                  Future.successful(
                    TooManyRequests(
                      Json.obj(
                        "errorCode" -> "TooManyRequests",
                        "nextAllowedAttemptTime" -> nextAllowedAttemptTime
                      )
                    )
                  )
              }
              .recover {
                case e =>
                  logger.error(s"Sign in error username = ${data.username}", e)
                  InternalServerError(Json.obj("errorCode" -> "SystemError"))
              }
          }
        )
    }

}
