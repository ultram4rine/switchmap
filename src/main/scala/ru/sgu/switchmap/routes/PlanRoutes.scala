package ru.sgu.switchmap.routes

import org.http4s.{Request, Response, StaticFile, Challenge}
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.{SwaggerSupport, SwaggerFileResponse}
import org.http4s.multipart.Multipart
import org.http4s.EntityDecoder
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import zio._
import zio.interop.catz._
import fs2.io.file.{Files, Path => Fs2Path}

final case class PlanRoutes[R <: Has[Authorizer]]() {
  val api: RhoRoutes[AppTask] = new RhoRoutes[AppTask] {
    val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
    import swaggerIO._

    "Get plan" **
      GET / "plans" / pv"planName" >>> AuthContext.auth |>> {
        (req: Request[AppTask], planName: String, auth: AuthStatus.Status) =>
          auth match {
            case AuthStatus.Succeed =>
              Ok(
                SwaggerFileResponse(
                  Files[AppTask].readAll(Fs2Path(s"./plans/${planName}"))
                )
              )
            case _ =>
              Unauthorized(())
          }
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
                case None => BadRequest("No file")
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
}
