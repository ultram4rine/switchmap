package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{Authorizer, AuthContext, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.{BuildRequest, BuildResponse}
import ru.sgu.switchmap.repositories._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._

final case class BuildRoutes[
  R <: Has[Authorizer] with BuildRepository
]() {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AppTask, A] =
    jsonEncoderOf[AppTask, A]

  val getBuildsEndpoint = endpoint.get
    .in("builds")
    .errorOut(stringBody)
    .out(jsonBody[List[BuildResponse]])
    .zServerLogic { _ => getBuilds().mapError(_.toString()) }
  val getBuildEndpoint = endpoint.get
    .in("builds" / path[String]("shortName"))
    .errorOut(stringBody)
    .out(jsonBody[BuildResponse])
    .zServerLogic { shortName =>
      getBuild(shortName).mapError(_.toString())
    }
  val addBuildEndpoint = endpoint.post
    .in("builds")
    .in(jsonBody[BuildRequest])
    .errorOut(stringBody)
    .out(plainBody[Boolean])
    .zServerLogic { build => createBuild(build).mapError(_.toString()) }
  val updateBuildEndpoint = endpoint.put
    .in("builds" / path[String]("shortName"))
    .in(jsonBody[BuildRequest])
    .errorOut(stringBody)
    .out(plainBody[Boolean])
    .zServerLogic { case (shortName, build) =>
      updateBuild(shortName, build).mapError(_.toString())
    }
  val deleteBuildEndpoint = endpoint.delete
    .in("builds" / path[String]("shortName"))
    .errorOut(stringBody)
    .out(plainBody[Boolean])
    .zServerLogic { shortName =>
      (getBuild(shortName) *> deleteBuild(shortName)).mapError(_.toString())
    }

  val routes = ZHttp4sServerInterpreter()
    .from(
      List(
        getBuildsEndpoint,
        getBuildEndpoint,
        addBuildEndpoint,
        updateBuildEndpoint,
        deleteBuildEndpoint
      )
    )
    .toRoutes

  val api: RhoRoutes[AppTask] =
    new RhoRoutes[AppTask] {
      val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
      import swaggerIO._

      "Get all builds" **
        GET / "builds" >>> AuthContext.auth |>> { auth: AuthStatus.Status =>
          auth match {
            case AuthStatus.Succeed =>
              getBuilds().foldM(_ => NotFound(()), Ok(_))
            case _ => Unauthorized(())
          }
        }

      "Get build by short name" **
        GET / "builds" / pv"shortName" >>> AuthContext.auth |>> {
          (shortName: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getBuild(shortName).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Add build" **
        POST / "builds" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          BuildRequest
        ] |>> { (auth: AuthStatus.Status, build: BuildRequest) =>
          auth match {
            case AuthStatus.Succeed =>
              createBuild(build).foldM(
                e => InternalServerError(e.getMessage),
                Created(_)
              )
            case _ => Unauthorized(())
          }
        }

      "Update build" **
        PUT / "builds" / pv"shortName" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          BuildRequest
        ] |>> {
          (shortName: String, auth: AuthStatus.Status, build: BuildRequest) =>
            auth match {
              case AuthStatus.Succeed =>
                updateBuild(shortName, build).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Delete build" **
        DELETE / "builds" / pv"shortName" >>> AuthContext.auth |>> {
          (shortName: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                (getBuild(shortName) *> deleteBuild(shortName))
                  .foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }
    }
}
