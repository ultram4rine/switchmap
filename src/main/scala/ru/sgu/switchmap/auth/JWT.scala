package ru.sgu.switchmap.auth

import zio._
import pdi.jwt.{JwtCirce, JwtAlgorithm, JwtClaim}

import ru.sgu.switchmap.config.AppConfig

trait JWT {
  def create(claim: JwtClaim): UIO[String]
}

case class JWTLive(cfg: AppConfig) extends JWT {
  override def create(claim: JwtClaim): UIO[String] =
    UIO.succeed(
      JwtCirce.encode(claim, cfg.jwtKey, JwtAlgorithm.HS256)
    )
}

object JWTLive {
  val layer: RLayer[Has[AppConfig], Has[JWT]] = (JWTLive(_)).toLayer
}

object JWT {
  def create(
    claim: JwtClaim
  ): RIO[Has[JWT], String] =
    ZIO.serviceWith[JWT](_.create(claim))
}
