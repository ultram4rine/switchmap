package ru.sgu.switchmap.auth

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import ru.sgu.switchmap.config.AppConfig
import zio._

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
  val layer: RLayer[AppConfig, JWT] = (JWTLive(_)).toLayer
}

object JWT {
  def create(
    claim: JwtClaim
  ): URIO[JWT, String] =
    ZIO.serviceWithZIO[JWT](_.create(claim))

  def validate(token: String): RIO[JWT, JwtClaim] =
    ZIO.serviceWithZIO[JWT](_.validate(token))
}
