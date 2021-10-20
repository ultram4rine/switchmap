package ru.sgu.switchmap.auth

import zio._

trait Authorizer {
  def authorize(token: AuthToken): Task[AuthStatus.Status]
}

object AuthStatus extends Enumeration {
  type Status = Value
  val Succeed, Failed, NoToken = Value
}

case class AuthorizerLive(jwt: JWT) extends Authorizer {
  override def authorize(token: AuthToken): Task[AuthStatus.Status] =
    jwt
      .validate(token.token)
      .fold(_ => AuthStatus.Failed, _ => AuthStatus.Succeed)
}

object AuthorizerLive {
  val layer: RLayer[Has[JWT], Has[Authorizer]] =
    (AuthorizerLive(_)).toLayer
}

object Authorizer {
  def authorize(token: AuthToken): RIO[Has[Authorizer], AuthStatus.Status] =
    ZIO.serviceWith[Authorizer](_.authorize(token))
}
