package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.rho.RhoRoutes
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

  object Auth extends org.http4s.rho.AuthedContext[FloorTask, AuthInfo]

  val api: RhoRoutes[FloorTask] = new RhoRoutes[FloorTask] {
    val swaggerIO = org.http4s.rho.swagger.SwaggerSupport[FloorTask]
    import swaggerIO._

    "Get all floors of build" **
      GET / "builds" / pv"shortName" / "floors" >>> Auth.auth |>> {
      (shortName: String, au: AuthInfo) =>
        getFloorsOf(shortName).foldM(_ => NotFound(()), Ok(_))
    }

    "Get floor of build by number" **
      POST / "builds" / pv"shortName" / "floors" / pathVar[Int](
      "number",
      "Number of floor"
    ) >>> Auth.auth |>> { (shortName: String, number: Int, au: AuthInfo) =>
      getFloor(shortName, number).foldM(_ => NotFound(()), Ok(_))
    }

    "Add floor to build" **
      POST / "builds" / pv"shortName" >>> Auth.auth ^ jsonOf[
      FloorTask,
      DBFloor
    ] |>> { (shortName: String, au: AuthInfo, floor: DBFloor) =>
      createFloor(floor).foldM(
        e => InternalServerError(e.getMessage()),
        Created(_)
      )
    }

    "Delete floor of build by number" **
      DELETE / "builds" / pv"shortName" / "floors" / pathVar[Int](
      "number",
      "Number of floor"
    ) >>> Auth.auth |>> { (shortName: String, number: Int, au: AuthInfo) =>
      (getFloor(shortName, number) *> deleteFloor(shortName, number))
        .foldM(_ => NotFound(()), Ok(_))
    }
  }
}
