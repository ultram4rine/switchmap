package ru.sgu.switchmap.auth

import java.time.Instant

import zio._
import pdi.jwt.JwtClaim

trait Authenticator {
  def authenticate(username: String, password: String): Task[AuthToken]
}

case class AuthToken(token: String)

case class AuthenticatorLive(ldap: LDAP, jwt: JWT) extends Authenticator {

  trait AuthenticationError extends Throwable

  val authenticationError: AuthenticationError = new AuthenticationError {
    override def getMessage: String = "Authentication Error"
  }

  override def authenticate(
    username: String,
    password: String
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
                  expiration =
                    Some(Instant.now.plusSeconds(157784760).getEpochSecond),
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
    password: String
  ): RIO[Has[Authenticator], AuthToken] =
    ZIO.serviceWith[Authenticator](_.authenticate(username, password))
}
