package controllers

import utils.auth.{DefaultEnv, User}
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Silhouette}
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{InjectedController, Request}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractAuthController(
  silhouette: Silhouette[DefaultEnv],
  configuration: Configuration,
  clock: Clock
)(implicit ex: ExecutionContext)
    extends InjectedController {

  protected def authenticateUser(
    user: User,
    loginInfo: LoginInfo,
    rememberMe: Boolean
  )(implicit request: Request[_]): Future[AuthenticatorResult] = {
    val c = configuration.underlying
    silhouette.env.authenticatorService
      .create(loginInfo)
      .map {
        case authenticator if rememberMe =>
          authenticator.copy(
            expirationDateTime = clock.now.plus(
              c.as[FiniteDuration](
                "silhouette.authenticator.rememberMe.authenticatorExpiry"
              ).toMillis
            ),
            idleTimeout = c.getAs[FiniteDuration](
              "silhouette.authenticator.rememberMe.authenticatorIdleTimeout"
            )
          )
        case authenticator => authenticator
      }
      .flatMap { authenticator =>
        silhouette.env.eventBus.publish(LoginEvent(user, request))
        silhouette.env.authenticatorService.init(authenticator).flatMap {
          token =>
            silhouette.env.authenticatorService.embed(
              token,
              Ok(
                Json.obj(
                  "username" -> user.username
                )
              )
            )
        }
      }
  }

}
