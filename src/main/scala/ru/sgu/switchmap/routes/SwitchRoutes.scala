package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.{
  SwitchRequest,
  SwitchResponse,
  SwitchResult,
  SavePositionRequest
}
import ru.sgu.switchmap.repositories._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._

final case class SwitchRoutes[R <: Has[Authorizer] with SwitchRepository]() {
  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  import ru.sgu.switchmap.json._
  import ru.sgu.switchmap.routes.schemas._

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AppTask, A] =
    jsonEncoderOf[AppTask, A]

  val runSyncEndpoint =
    endpoint.get.in("switches" / "sync").errorOut(stringBody).zServerLogic {
      _ => sync().mapError(_.toString())
    }
  val getSNMPCommunitiesEndpoint = endpoint.get
    .in("switches" / "snmp" / "communities")
    .errorOut(stringBody)
    .out(jsonBody[List[String]])
    .zServerLogic { _ => snmpCommunities().mapError(_.toString()) }
  val getSwitchesEndpoint = endpoint.get
    .in("switches")
    .errorOut(stringBody)
    .out(jsonBody[List[SwitchResponse]])
    .zServerLogic { _ => getSwitches().mapError(_.toString()) }
  val getSwitchesOfBuildEndpoint = endpoint.get
    .in("builds" / path[String]("shortName") / "switches")
    .errorOut(stringBody)
    .out(jsonBody[List[SwitchResponse]])
    .zServerLogic { shortName =>
      getSwitchesOf(shortName).mapError(_.toString())
    }
  val getSwitchesOfFloorEndpoint = endpoint.get
    .in(
      "builds" / path[String]("shortName") /
        "floors" / path[Int]("number") / "switches"
    )
    .errorOut(stringBody)
    .out(jsonBody[List[SwitchResponse]])
    .zServerLogic { case (shortName, number) =>
      getSwitchesOf(shortName, number).mapError(_.toString())
    }
  val getSwitchEndpoint = endpoint.get
    .in("switches" / path[String]("name"))
    .errorOut(stringBody)
    .out(jsonBody[SwitchResponse])
    .zServerLogic { name =>
      getSwitch(name).mapError(_.toString())
    }
  val addSwitchEndpoint = endpoint.post
    .in("switches")
    .in(jsonBody[SwitchRequest])
    .errorOut(stringBody)
    .out(jsonBody[SwitchResult])
    .zServerLogic { switch =>
      createSwitch(switch).mapError(_.toString())
    }
  val updateSwitchEndpoint = endpoint.put
    .in("switches" / path[String]("name"))
    .in(jsonBody[SwitchRequest])
    .errorOut(stringBody)
    .out(jsonBody[SwitchResult])
    .zServerLogic { case (name, switch) =>
      updateSwitch(name, switch).mapError(_.toString())
    }
  val updateSwitchPositionEndpoint = endpoint.patch
    .in("switches" / path[String]("name"))
    .in(jsonBody[SavePositionRequest])
    .errorOut(stringBody)
    .out(plainBody[Boolean])
    .zServerLogic { case (name, position) =>
      updateSwitchPosition(name, position).mapError(_.toString())
    }
  val deleteSwitchEndpoint = endpoint.delete
    .in("switches" / path[String]("name"))
    .errorOut(stringBody)
    .out(plainBody[Boolean])
    .zServerLogic { name =>
      (getSwitch(name) *> deleteSwitch(name)).mapError(_.toString())
    }

  val api: RhoRoutes[AppTask] =
    new RhoRoutes[AppTask] {
      val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
      import swaggerIO._

      import ru.sgu.switchmap.json._

      "Run switches sync with network" **
        GET / "switches" / "sync" >>> AuthContext.auth |>> {
          auth: AuthStatus.Status =>
            auth match {
              case AuthStatus.Succeed =>
                sync().foldM(_ => InternalServerError(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

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
