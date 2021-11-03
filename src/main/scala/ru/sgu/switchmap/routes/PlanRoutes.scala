package ru.sgu.switchmap.routes

import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{HttpRoutes, Request, Response, StaticFile, Challenge}
import org.http4s.circe._
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.multipart.Multipart
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import zio._
import zio.interop.catz._
import fs2.io.file.{Files, Path => Fs2Path}

final case class PlanRoutes[R <: Has[Authorizer]]() {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  val api: RhoRoutes[AppTask] = new RhoRoutes[AppTask] {
    val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
    import swaggerIO._

    "Get plan" **
      GET / "plans" / pv"planName" >>> AuthContext.auth |>> {
        (req: Request[AppTask], planName: String, auth: AuthStatus.Status) =>
          fetchResource(planName, req, auth)
      }

    "Upload plan" **
      POST / "plans" / pv"shortName" / pathVar[Int](
        "number",
        "Number of floor"
      ) >>> AuthContext.auth ^ EntityDecoder.multipart[AppTask] |>> {
        (
          shortName: String,
          number: Int,
          auth: AuthStatus.Status,
          m: Multipart[AppTask]
        ) =>
          auth match {
            case AuthStatus.Succeed =>
              m.parts.find(_.name == Some("planFile")) match {
                case None => BadRequest(s"Not file")
                case Some(part) => {
                  val stream = part.body
                    .through(
                      Files[AppTask].writeAll(
                        Fs2Path(
                          s"./plans/${shortName}f${number}.png"
                        )
                      )
                    )

                  Created(
                    stream.map(_ => "Multipart file parsed successfully")
                  )
                }

              }
            case _ =>
              Unauthorized(())
          }
      }
  }

  private def fetchResource(
    planName: String,
    req: Request[AppTask],
    auth: AuthStatus.Status
  ): AppTask[Response[AppTask]] = {
    auth match {
      case AuthStatus.Succeed =>
        StaticFile
          .fromPath(Fs2Path(s"./plans/${planName}"), Some(req))
          .getOrElseF(NotFound(()))
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
  }
}
