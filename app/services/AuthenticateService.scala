package services

import java.time.Instant

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import auth.User
import auth.ldap.LDAPProvider
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.{
  IdentityNotFoundException,
  InvalidPasswordException
}
import javax.inject.{Inject, Named}
import services.BruteForceDefenderActor.{
  IsSignInAllowed,
  RegisterWrongPasswordSignIn,
  SignInAllowed,
  SignInForbidden
}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AuthenticateService @Inject() (
  credentialsProvider: LDAPProvider,
  userService: UserService,
  @Named("brute-force-defender") bruteForceDefenderActor: ActorRef
)(implicit ec: ExecutionContext) {

  implicit val timeout: Timeout = 5.seconds

  def credentials(
    username: String,
    password: String
  ): Future[AuthenticateResult] = {
    (bruteForceDefenderActor ? IsSignInAllowed(username)).flatMap {
      case SignInAllowed(attemptsAllowed) =>
        val credentials = Credentials(username, password)
        credentialsProvider
          .authenticate(credentials)
          .flatMap { loginInfo =>
            userService.retrieve(loginInfo).map {
              case Some(user) =>
                Success(user)
              case None =>
                UserNotFound
            }
          }
          .recoverWith {
            case _: InvalidPasswordException =>
              // TODO refactor this. Put InvalidCredentials event to Silhouette's EventBus and listen to it in BruteForceDefenderActor
              bruteForceDefenderActor ! RegisterWrongPasswordSignIn(username)
              Future.successful(InvalidPassword(attemptsAllowed))
            case _: IdentityNotFoundException =>
              Future.successful(UserNotFound)
            case e =>
              Future.failed(e)
          }
      case SignInForbidden(nextSignInAllowedAt) =>
        Future.successful(ToManyAuthenticateRequests(nextSignInAllowedAt))
    }
  }

}

sealed trait AuthenticateResult
case class Success(user: User) extends AuthenticateResult
case class InvalidPassword(attemptsAllowed: Int) extends AuthenticateResult
object UserNotFound extends AuthenticateResult
case class ToManyAuthenticateRequests(nextAllowedAttemptTime: Instant)
    extends AuthenticateResult
