package ru.sgu.switchmap.auth

import cats.data.{Kleisli, OptionT}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.Request
import org.typelevel.ci.CIString
import ru.sgu.switchmap.Main.AppTask
import zio._
import zio.interop.catz._

object Middleware {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]

  private def getToken(
    request: Request[AppTask]
  ): AppTask[AuthStatus.Status] =
    request.headers.get(CIString("X-Auth-Token")) match {
      case Some(token) =>
        Authorizer
          .authorize(AuthToken(token.head.value))
      case None => {
        request.cookies.find(_.name == "X-Auth-Token") match {
          case Some(c) =>
            Authorizer
              .authorize(AuthToken(c.content))
          case None => ZIO.succeed(AuthStatus.NoToken)
        }
      }
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
