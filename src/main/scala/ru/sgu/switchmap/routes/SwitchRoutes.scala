package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.{SwitchRequest, SavePositionRequest}
import ru.sgu.switchmap.repositories._
import zio._
import zio.interop.catz._

final case class SwitchRoutes[R <: Has[Authorizer] with SwitchRepository]() {
  type SwitchTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AppTask, A] =
    jsonEncoderOf[AppTask, A]

  val api: RhoRoutes[AppTask] =
    new RhoRoutes[AppTask] {
      val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
      import swaggerIO._

      import ru.sgu.switchmap.utils.JSONUtil._

      "Get SNMP communitites" **
        GET / "switches" / "snmp" / "communities" >>> AuthContext.auth |>> {
          auth: AuthStatus.Status =>
            auth match {
              case AuthStatus.Succeed =>
                snmpCommunities().foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Get all switches" **
        GET / "switches" >>> AuthContext.auth |>> { auth: AuthStatus.Status =>
          auth match {
            case AuthStatus.Succeed =>
              getSwitches().foldM(_ => NotFound(()), Ok(_))
            case _ => Unauthorized(())
          }
        }

      "Get all switches of build" **
        GET / "builds" / pv"shortName" / "switches" >>> AuthContext.auth |>> {
          (shortName: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getSwitchesOf(shortName).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Get all switches of floor in build" **
        GET / "builds" / pv"shortName" / "floors" / pathVar[Int](
          "number",
          "Number of floor"
        ) / "switches" >>> AuthContext.auth |>> {
          (shortName: String, number: Int, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getSwitchesOf(shortName, number).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Get switch by name" **
        GET / "switches" / pv"name" >>> AuthContext.auth |>> {
          (name: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getSwitch(name).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Add switch" **
        POST / "switches" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          SwitchRequest
        ] |>> { (auth: AuthStatus.Status, switch: SwitchRequest) =>
          auth match {
            case AuthStatus.Succeed =>
              createSwitch(switch).foldM(
                e => InternalServerError(e.getMessage),
                Created(_)
              )
            case _ => Unauthorized(())
          }
        }

      "Update switch" **
        PUT / "switches" / pv"name" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          SwitchRequest
        ] |>> {
          (name: String, auth: AuthStatus.Status, switch: SwitchRequest) =>
            auth match {
              case AuthStatus.Succeed =>
                updateSwitch(name, switch).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Update switch position" **
        PATCH / "switches" / pv"name" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          SavePositionRequest
        ] |>> {
          (
            name: String,
            auth: AuthStatus.Status,
            position: SavePositionRequest
          ) =>
            auth match {
              case AuthStatus.Succeed =>
                updateSwitchPosition(name, position).foldM(
                  _ => NotFound(()),
                  Ok(_)
                )
              case _ => Unauthorized(())
            }
        }

      "Delete switch" **
        DELETE / "switches" / pv"name" >>> AuthContext.auth |>> {
          (name: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                (getSwitch(name) *> deleteSwitch(name))
                  .foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }
    }
}
