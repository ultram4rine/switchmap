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
    withAuth.get.in("switches" / "sync").serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => sync().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getSNMPCommunitiesEndpoint = withAuth.get
    .in("switches" / "snmp" / "communities")
    .out(jsonBody[List[String]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => snmpCommunities().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getSwitchesEndpoint = withAuth.get
    .in("switches")
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => getSwitches().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getSwitchesOfBuildEndpoint = withAuth.get
    .in("builds" / path[String]("shortName") / "switches")
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getSwitchesOf(shortName).mapError(_.toString())
        case _ => ZIO.fail("401")
      }
    }
  val getSwitchesOfFloorEndpoint = withAuth.get
    .in(
      "builds" / path[String]("shortName") /
        "floors" / path[Int]("number") / "switches"
    )
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            getSwitchesOf(shortName, number).mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }
  val getSwitchEndpoint = withAuth.get
    .in("switches" / path[String]("name"))
    .out(jsonBody[SwitchResponse])
    .serverLogic { as => name =>
      as match {
        case AuthStatus.Succeed => getSwitch(name).mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val addSwitchEndpoint = withAuth.post
    .in("switches")
    .in(jsonBody[SwitchRequest])
    .out(jsonBody[SwitchResult])
    .serverLogic { as => switch =>
      as match {
        case AuthStatus.Succeed => createSwitch(switch).mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val updateSwitchEndpoint = withAuth.put
    .in("switches" / path[String]("name"))
    .in(jsonBody[SwitchRequest])
    .out(jsonBody[SwitchResult])
    .serverLogic { as =>
      { case (name, switch) =>
        as match {
          case AuthStatus.Succeed =>
            updateSwitch(name, switch).mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }
  val updateSwitchPositionEndpoint = withAuth.patch
    .in("switches" / path[String]("name"))
    .in(jsonBody[SavePositionRequest])
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (name, position) =>
        as match {
          case AuthStatus.Succeed =>
            updateSwitchPosition(name, position).mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }
  val deleteSwitchEndpoint = withAuth.delete
    .in("switches" / path[String]("name"))
    .out(plainBody[Boolean])
    .serverLogic { as => name =>
      as match {
        case AuthStatus.Succeed =>
          (getSwitch(name) *> deleteSwitch(name)).mapError(_.toString())
        case _ => ZIO.fail("401")
      }
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
