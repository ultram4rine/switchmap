package ru.sgu.switchmap.api

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.models._
import ru.sgu.switchmap.repositories._

final case class FloorAPI[R <: FloorRepository]() {

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

  def route: HttpRoutes[FloorTask] = {
    HttpRoutes.of[FloorTask] {
      case GET -> Root / "builds" / shortName / "floors" =>
        getFloorsOf(shortName).foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName / "floors" / IntVar(number) =>
        getFloor(shortName, number).foldM(_ => NotFound(), Ok(_))

      case req @ POST -> Root / "builds" / shortName / "floors" =>
        req.decode[Floor] { floor =>
          Created(createFloor(floor))
        }

      case DELETE -> Root / "builds" / shortName / "floors" / IntVar(number) =>
        (getFloor(shortName, number) *> deleteFloor(shortName, number))
          .foldM(_ => NotFound(), Ok(_))
    }
  }

  /* private def createFloorResource(f: Floor): IO[FloorResource] = {
    for {
      swNum <-
        switchRepository.getNumberOfSwitchesOfFloor(f.buildShortName, f.number)
    } yield FloorResource(f.number, swNum)
  } */
}

/* case class FloorResource(
  number: Int,
  switchesNumber: Int
) */
