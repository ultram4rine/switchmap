package ru.sgu.switchmap.auth

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import cats.data.{Kleisli, OptionT}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{AuthedRequest, Response, Status}
import org.http4s.server.AuthMiddleware
import org.http4s.Request /* AuthedRoutes, Challenge, */
import org.http4s.Challenge
import org.typelevel.ci.CIString
import ru.sgu.switchmap.Main.AppTask
import zio._
import zio.console.putStrLn
import zio.interop.catz._
import org.http4s.AuthedRoutes

object Middleware {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  private def getToken(
    request: Request[AppTask]
  ): AppTask[AuthStatus.Status] =
    request.headers.get(CIString("X-Auth-Token")) match {
      case Some(token) =>
        Authorizer
          .authorize(AuthToken(token.head.value))
      case None => ZIO.succeed(AuthStatus.NoToken)
    }

  def authUser
    : Kleisli[OptionT[AppTask, *], Request[AppTask], AuthStatus.Status] = {
    Kleisli { request =>
      OptionT.liftF(
        getToken(request)
      )
    }
  }

  val middleware: AuthMiddleware[AppTask, AuthStatus.Status] =
    AuthMiddleware.withFallThrough(authUser)
}
