package ru.sgu.switchmap.routes

import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{HttpRoutes, Request, Response, StaticFile, Challenge}
import org.http4s.circe._
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.DBFloor
import ru.sgu.switchmap.repositories._
import zio._
import zio.interop.catz._

final case class StaticRoutes[R <: Has[Authorizer]]() {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  val api: RhoRoutes[AppTask] = new RhoRoutes[AppTask] {
    val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
    import swaggerIO._

    "Get static file. Note that plans requires authorization" **
      GET / "static" / * >>> AuthContext.auth |>> {
      (req: Request[AppTask], _: List[String], auth: AuthStatus.Status) =>
        fetchResource(req.pathInfo.toString(), req, auth)
    }
  }

  private def fetchResource(
    path: String,
    req: Request[AppTask],
    auth: AuthStatus.Status
  ): AppTask[Response[AppTask]] = {
    val serv = StaticFile
      .fromResource(path, Some(req))
      .getOrElseF(NotFound(()))
    if (path.contains("/plans/")) {
      auth match {
        case AuthStatus.Succeed => serv
        case _ =>
          Unauthorized(
            `WWW-Authenticate`(
              Challenge(
                "X-Auth-Token",
                "SwitchMap",
                Map.empty
              )
            )
          )
      }
    } else serv
  }
}
