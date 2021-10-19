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
import ru.sgu.switchmap.models.Switch
import ru.sgu.switchmap.repositories._

final case class SwitchRoutes[R <: Has[Authorizer] with SwitchRepository]() {
  type SwitchTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[SwitchTask, A] = jsonOf[SwitchTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[SwitchTask, A] =
    jsonEncoderOf[SwitchTask, A]

  object Auth extends org.http4s.rho.AuthedContext[SwitchTask, AuthInfo]

  val api: RhoRoutes[SwitchTask] =
    new RhoRoutes[SwitchTask] {
      val swaggerIO = org.http4s.rho.swagger.SwaggerSupport[SwitchTask]
      import swaggerIO._

      "Get all switches" **
        GET / "switches" >>> Auth.auth |>> { au: AuthInfo =>
        getSwitches().foldM(_ => NotFound(()), Ok(_))
      }

      "Get all switches of build" **
        GET / "builds" / pv"shortName" / "switches" >>> Auth.auth |>> {
        (shortName: String, au: AuthInfo) =>
          getSwitchesOf(shortName).foldM(_ => NotFound(()), Ok(_))
      }

      "Get all switches of floor in build" **
        GET / "builds" / pv"shortName" / "floors" / pathVar[Int](
        "number",
        "Number of floor"
      ) / "switches" >>> Auth.auth |>> {
        (shortName: String, number: Int, au: AuthInfo) =>
          getSwitchesOf(shortName, number).foldM(_ => NotFound(()), Ok(_))
      }

      "Get switch by name" **
        GET / "switches" / pv"name" >>> Auth.auth |>> {
        (name: String, au: AuthInfo) =>
          getSwitch(name).foldM(_ => NotFound(()), Ok(_))
      }

      "Add switch" **
        POST / "switches" >>> Auth.auth ^ jsonOf[SwitchTask, Switch] |>> {
        (au: AuthInfo, switch: Switch) =>
          createSwitch(switch).foldM(
            e => InternalServerError(e.getMessage()),
            Created(_)
          )
      }

      "Update switch" **
        PUT / "switches" / pv"name" >>> Auth.auth ^ jsonOf[
        SwitchTask,
        Switch
      ] |>> { (name: String, au: AuthInfo, switch: Switch) =>
        updateSwitch(name, switch).foldM(_ => NotFound(()), Ok(_))
      }

      "Delete switch" **
        DELETE / "switches" / pv"name" >>> Auth.auth |>> {
        (name: String, au: AuthInfo) =>
          (getSwitch(name) *> deleteSwitch(name))
            .foldM(_ => NotFound(()), Ok(_))
      }
    }
}
