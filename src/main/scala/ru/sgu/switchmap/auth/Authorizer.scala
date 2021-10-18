package ru.sgu.switchmap.auth

import zio._

trait Authorizer {
  def authorize(token: AuthToken): Task[AuthInfo]
}

case class AuthInfo(status: String)

case class AuthorizerLive(jwt: JWT) extends Authorizer {
  override def authorize(token: AuthToken): Task[AuthInfo] =
    for {
      claims <- jwt.validate(token.token)
    } yield new AuthInfo("succeed")
}

object AuthorizerLive {
  val layer: RLayer[Has[JWT], Has[Authorizer]] =
    (AuthorizerLive(_)).toLayer
}

object Authorizer {
  def authorize(token: AuthToken): RIO[Has[Authorizer], AuthInfo] =
    ZIO.serviceWith[Authorizer](_.authorize(token))
}
