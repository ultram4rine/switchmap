package ru.sgu.switchmap.auth

import java.time.Instant
import ru.sgu.switchmap.models.AuthToken
import zio._

trait Authorizer {
  def authorize(token: AuthToken): Task[AuthStatus.Status]
}

object AuthStatus extends Enumeration {
  type Status = Value
  val Succeed, Failed, Expired, NoToken = Value
}

case class AuthorizerLive(jwt: JWT) extends Authorizer {
  override def authorize(token: AuthToken): Task[AuthStatus.Status] = {
    for {
      claims <- jwt
        .validate(token.token)
      exp = claims.expiration.getOrElse(-1.toLong)
    } yield
      if (exp == -1) AuthStatus.NoToken
      else if (exp < Instant.now().getEpochSecond()) AuthStatus.Expired
      else AuthStatus.Succeed
  }.fold(_ => AuthStatus.Failed, ok => ok)
}

object AuthorizerLive {
  val layer: RLayer[Has[JWT], Has[Authorizer]] =
    (AuthorizerLive(_)).toLayer
}

object Authorizer {
  def authorize(token: AuthToken): RIO[Has[Authorizer], AuthStatus.Status] =
    ZIO.serviceWith[Authorizer](_.authorize(token))
}
