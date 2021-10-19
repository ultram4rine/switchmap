package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, AuthedRoutes}
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.auth.{AuthInfo, Authorizer}
import ru.sgu.switchmap.models.DBFloor
import ru.sgu.switchmap.repositories._

final case class FloorRoutes[R <: Has[Authorizer] with FloorRepository]() {
  type FloorTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[FloorTask, A] = jsonOf[FloorTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[FloorTask, A] =
    jsonEncoderOf[FloorTask, A]

  val dsl: Http4sDsl[FloorTask] = Http4sDsl[FloorTask]
  import dsl._

  def route: AuthedRoutes[AuthInfo, FloorTask] = {
    AuthedRoutes.of[AuthInfo, FloorTask] {
      case GET -> Root / "builds" / shortName / "floors" as authToken =>
        getFloorsOf(shortName).foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName / "floors" / IntVar(
            number
          ) as authToken =>
        getFloor(shortName, number).foldM(_ => NotFound(), Ok(_))

      case req @ POST -> Root / "builds" / shortName / "floors" as authToken =>
        req.req.decode[DBFloor] { floor =>
          Created(createFloor(floor))
        }

      case DELETE -> Root / "builds" / shortName / "floors" / IntVar(
            number
          ) as authToken =>
        (getFloor(shortName, number) *> deleteFloor(shortName, number))
          .foldM(_ => NotFound(), Ok(_))
    }
  }
}
