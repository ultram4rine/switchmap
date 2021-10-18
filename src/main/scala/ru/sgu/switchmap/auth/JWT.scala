package ru.sgu.switchmap.auth

import zio._
import pdi.jwt.{JwtCirce, JwtAlgorithm, JwtClaim}

import ru.sgu.switchmap.config.AppConfig

trait JWT {
  def create(claim: JwtClaim): UIO[String]
  def validate(token: String): Task[JwtClaim]
}

case class JWTLive(cfg: AppConfig) extends JWT {
  override def create(claim: JwtClaim): UIO[String] =
    UIO.succeed(
      JwtCirce.encode(claim, cfg.jwtKey, JwtAlgorithm.HS256)
    )

  override def validate(token: String): Task[JwtClaim] =
    Task.fromTry(JwtCirce.decode(token, cfg.jwtKey, Seq(JwtAlgorithm.HS256)))
}

object JWTLive {
  val layer: RLayer[Has[AppConfig], Has[JWT]] = (JWTLive(_)).toLayer
}

object JWT {
  def create(
    claim: JwtClaim
  ): URIO[Has[JWT], String] =
    ZIO.serviceWith[JWT](_.create(claim))

  def validate(token: String): RIO[Has[JWT], JwtClaim] =
    ZIO.serviceWith[JWT](_.validate(token))
}
