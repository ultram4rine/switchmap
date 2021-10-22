package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
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

final case class FloorRoutes[R <: Has[Authorizer] with FloorRepository]() {
  type FloorTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AppTask, A] =
    jsonEncoderOf[AppTask, A]

  val api: RhoRoutes[AppTask] = new RhoRoutes[AppTask] {
    val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
    import swaggerIO._

    "Get all floors of build" **
      GET / "builds" / pv"shortName" / "floors" >>> AuthContext.auth |>> {
      (shortName: String, auth: AuthStatus.Status) =>
        auth match {
          case AuthStatus.Succeed =>
            getFloorsOf(shortName).foldM(_ => NotFound(()), Ok(_))
          case _ => Unauthorized(())
        }
    }

    "Get floor of build by number" **
      POST / "builds" / pv"shortName" / "floors" / pathVar[Int](
      "number",
      "Number of floor"
    ) >>> AuthContext.auth |>> {
      (shortName: String, number: Int, auth: AuthStatus.Status) =>
        auth match {
          case AuthStatus.Succeed =>
            getFloor(shortName, number).foldM(_ => NotFound(()), Ok(_))
          case _ => Unauthorized(())
        }
    }

    "Add floor to build" **
      POST / "builds" / pv"shortName" >>> AuthContext.auth ^ jsonOf[
      AppTask,
      DBFloor
    ] |>> { (_: String, auth: AuthStatus.Status, floor: DBFloor) =>
      auth match {
        case AuthStatus.Succeed =>
          createFloor(floor).foldM(
            e => InternalServerError(e.getMessage),
            Created(_)
          )
        case _ => Unauthorized(())
      }
    }

    "Delete floor of build by number" **
      DELETE / "builds" / pv"shortName" / "floors" / pathVar[Int](
      "number",
      "Number of floor"
    ) >>> AuthContext.auth |>> {
      (shortName: String, number: Int, auth: AuthStatus.Status) =>
        auth match {
          case AuthStatus.Succeed =>
            (getFloor(shortName, number) *> deleteFloor(shortName, number))
              .foldM(_ => NotFound(()), Ok(_))
          case _ => Unauthorized(())
        }
    }
  }
}
