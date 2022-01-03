package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{Authorizer, AuthStatus}
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
import zio.logging.Logging

final case class SwitchRoutes[R <: Has[
  Authorizer
] with SwitchRepository with Logging]() {
  import ru.sgu.switchmap.json._
  import ru.sgu.switchmap.routes.schemas._

  private[this] val switchBaseEndpoint = secureEndpoint.tag("switches")

  val runSyncEndpoint =
    switchBaseEndpoint.get.in("switches" / "sync").serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => sync().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getSNMPCommunitiesEndpoint = switchBaseEndpoint.get
    .in("switches" / "snmp" / "communities")
    .out(jsonBody[List[String]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => snmpCommunities().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getSwitchesEndpoint = switchBaseEndpoint.get
    .in("switches")
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => getSwitches().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getSwitchesOfBuildEndpoint = switchBaseEndpoint.get
    .in("builds" / path[String]("shortName") / "switches")
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getSwitchesOf(shortName).mapError(_.toString())
        case _ => ZIO.fail("401")
      }
    }
  val getSwitchesOfFloorEndpoint = switchBaseEndpoint.get
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
  val getSwitchEndpoint = switchBaseEndpoint.get
    .in("switches" / path[String]("name"))
    .out(jsonBody[SwitchResponse])
    .serverLogic { as => name =>
      as match {
        case AuthStatus.Succeed => getSwitch(name).mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val addSwitchEndpoint = switchBaseEndpoint.post
    .in("switches")
    .in(jsonBody[SwitchRequest])
    .out(jsonBody[SwitchResult])
    .serverLogic { as => switch =>
      as match {
        case AuthStatus.Succeed => createSwitch(switch).mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val updateSwitchEndpoint = switchBaseEndpoint.put
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
  val updateSwitchPositionEndpoint = switchBaseEndpoint.patch
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
  val deleteSwitchEndpoint = switchBaseEndpoint.delete
    .in("switches" / path[String]("name"))
    .out(plainBody[Boolean])
    .serverLogic { as => name =>
      as match {
        case AuthStatus.Succeed =>
          (getSwitch(name) *> deleteSwitch(name)).mapError(_.toString())
        case _ => ZIO.fail("401")
      }
    }

  val routes = List(
    runSyncEndpoint.widen[R],
    getSNMPCommunitiesEndpoint.widen[R],
    getSwitchesEndpoint.widen[R],
    getSwitchesOfBuildEndpoint.widen[R],
    getSwitchesOfFloorEndpoint.widen[R],
    getSwitchEndpoint.widen[R],
    addSwitchEndpoint.widen[R],
    updateSwitchEndpoint.widen[R],
    updateSwitchPositionEndpoint.widen[R],
    deleteSwitchEndpoint.widen[R]
  )
}
