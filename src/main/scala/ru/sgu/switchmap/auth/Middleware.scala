package ru.sgu.switchmap.auth

import cats.data.{Kleisli, OptionT}
import org.http4s.{AuthedRoutes, Request, Challenge}
import org.http4s.syntax.header._
import org.http4s.headers.{Authorization, `WWW-Authenticate`}
import org.http4s.server.AuthMiddleware
import org.http4s._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.Main.AppTask

object Middleware {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  private def getToken(
    request: Request[AppTask]
  ): AppTask[Either[Throwable, AuthInfo]] = {
    val token =
      for {
        header <-
          request.headers
            .get[Authorization]
            .map(_.value)
        asSplit = header.split(" ")
        if asSplit.size == 2
      } yield asSplit(1)
    token
      .map { value =>
        val tok = Authorizer.authorize(AuthToken(value))
        tok.either
      }
      .getOrElse(IO.succeed(Left(new Exception("Unauthenticated"))))
  }

  def authUser: Kleisli[AppTask, Request[AppTask], Either[String, AuthInfo]] = {
    Kleisli({ request =>
      getToken(request).map { e => e.left.map(_.toString()) }
    })
  }

  val onFailure: AuthedRoutes[String, AppTask] = Kleisli(req =>
    OptionT.liftF {
      Unauthorized(
        `WWW-Authenticate`(
          Challenge(
            "Authorization: Bearer",
            "SwitchMap",
            Map.empty
          )
        )
      )
    }
  )

  val middleware: AuthMiddleware[AppTask, AuthInfo] =
    AuthMiddleware(authUser, onFailure)
}
