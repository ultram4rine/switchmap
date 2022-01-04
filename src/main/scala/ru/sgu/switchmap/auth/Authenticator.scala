package ru.sgu.switchmap.auth

import java.time.Instant

import zio._
import pdi.jwt.JwtClaim
import ru.sgu.switchmap.models.AuthToken
import sttp.tapir.Schema.annotations._

trait Authenticator {
  def authenticate(
    username: String,
    password: String,
    rememberMe: Boolean
  ): Task[AuthToken]
}

case class AuthenticatorLive(ldap: LDAP, jwt: JWT) extends Authenticator {

  trait AuthenticationError extends Throwable

  val authenticationError: AuthenticationError = new AuthenticationError {
    override def getMessage: String = "Authentication Error"
  }

  override def authenticate(
    username: String,
    password: String,
    rememberMe: Boolean
  ): Task[AuthToken] =
    ldap.findUser(username).flatMap {
      case true =>
        val conn = ldap.connect(username, password)
        conn.foldM(
          _ => IO.fail(authenticationError),
          _ =>
            for {
              token <- jwt.create(
                JwtClaim(
                  issuer = Some("SwitchMap"),
                  expiration = Some(
                    Instant.now
                      .plusSeconds(if (!rememberMe) 259200 else 864000)
                      .getEpochSecond
                  ),
                  issuedAt = Some(Instant.now.getEpochSecond)
                )
              )
            } yield AuthToken(token)
        )
      case false => IO.fail(authenticationError)
    }
}

object AuthenticatorLive {
  val layer: RLayer[Has[LDAP] with Has[JWT], Has[Authenticator]] =
    (AuthenticatorLive(_, _)).toLayer
}

object Authenticator {
  def authenticate(
    username: String,
    password: String,
    rememberMe: Boolean
  ): RIO[Has[Authenticator], AuthToken] =
    ZIO.serviceWith[Authenticator](
      _.authenticate(username, password, rememberMe)
    )
}
