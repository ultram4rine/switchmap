package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, AuthedRoutes}
import org.http4s.rho.RhoRoutes
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.auth.{AuthInfo, Authorizer}
import ru.sgu.switchmap.models.DBBuild
import ru.sgu.switchmap.repositories._

final case class BuildRoutes[
  R <: Has[Authorizer] with BuildRepository
]() {
  type BuildTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[BuildTask, A] = jsonOf[BuildTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[BuildTask, A] =
    jsonEncoderOf[BuildTask, A]

  val dsl: Http4sDsl[BuildTask] = Http4sDsl[BuildTask]
  import dsl._

  object Auth extends org.http4s.rho.AuthedContext[BuildTask, AuthInfo]

  val api: RhoRoutes[BuildTask] =
    new RhoRoutes[BuildTask] {
      val swaggerIO = org.http4s.rho.swagger.SwaggerSupport[BuildTask]
      import swaggerIO._

      "Get all builds" **
        GET / "builds" >>> Auth.auth |>> { au: AuthInfo =>
        getBuilds().foldM(_ => NotFound(()), Ok(_))
      }

      "Get build by short name" **
        GET / "builds" / pv"shortName" >>> Auth.auth |>> {
        (shortName: String, au: AuthInfo) =>
          getBuild(shortName).foldM(_ => NotFound(()), Ok(_))
      }

      "Add build" **
        POST / "builds" >>> Auth.auth ^ jsonOf[BuildTask, DBBuild] |>> {
        (au: AuthInfo, build: DBBuild) =>
          createBuild(build).foldM(
            e => InternalServerError(e.getMessage()),
            Created(_)
          )
      }

      "Update build" **
        PUT / "builds" / pv"shortName" >>> Auth.auth ^ jsonOf[
        BuildTask,
        DBBuild
      ] |>> { (shortName: String, au: AuthInfo, build: DBBuild) =>
        updateBuild(shortName, build).foldM(_ => NotFound(()), Ok(_))
      }

      "Delete build" **
        DELETE / "builds" / pv"shortName" >>> Auth.auth |>> {
        (shortName: String, au: AuthInfo) =>
          (getBuild(shortName) *> deleteBuild(shortName))
            .foldM(_ => NotFound(()), Ok(_))
      }
    }
}
