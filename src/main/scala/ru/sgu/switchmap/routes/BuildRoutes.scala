package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, AuthedRoutes}
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

  def route: AuthedRoutes[AuthInfo, BuildTask] = {
    AuthedRoutes.of[AuthInfo, BuildTask] {
      case GET -> Root / "builds" as authToken =>
        getBuilds().foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName as authToken =>
        getBuild(shortName).foldM(_ => NotFound(), Ok(_))

      case req @ POST -> Root / "builds" as authToken =>
        req.req.decode[DBBuild] { build =>
          Created(createBuild(build))
        }

      case req @ PUT -> Root / "builds" / shortName as authToken =>
        req.req.decode[DBBuild] { build =>
          updateBuild(shortName, build).foldM(_ => NotFound(), Ok(_))
        }

      case DELETE -> Root / "builds" / shortName as authToken =>
        (getBuild(shortName) *> deleteBuild(shortName))
          .foldM(_ => NotFound(), Ok(_))
    }
  }
}
