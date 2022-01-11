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
import sttp.model.StatusCode

final case class SwitchRoutes[R <: Has[
  Authorizer
] with SwitchRepository with Logging]() {
  import ru.sgu.switchmap.json._
  import ru.sgu.switchmap.routes.schemas._

  private[this] val switchBaseEndpoint = secureEndpoint.tag("switches")

  val runSyncEndpoint =
    switchBaseEndpoint
      .summary("Run switches sync with network")
      .get
      .in("switches" / "sync")
      .serverLogic { as => _ =>
        as match {
          case AuthStatus.Succeed => sync().mapError(_.toString())
          case _                  => ZIO.fail(())
        }
      }

  val getSNMPCommunitiesEndpoint = switchBaseEndpoint
    .summary("Get SNMP communitites")
    .get
    .in("switches" / "snmp" / "communities")
    .out(jsonBody[List[String]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => snmpCommunities().mapError(_.toString())
        case _                  => ZIO.fail(())
      }
    }

  val getSwitchesEndpoint = switchBaseEndpoint
    .summary("Get all switches")
    .get
    .in("switches")
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => getSwitches().mapError(_.toString())
        case _                  => ZIO.fail(())
      }
    }

  val getSwitchesOfBuildEndpoint = switchBaseEndpoint
    .summary("Get all switches of build")
    .get
    .in("builds" / path[String]("shortName") / "switches")
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build not found")
      )
    )
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getSwitchesOf(shortName).mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val getSwitchesOfFloorEndpoint = switchBaseEndpoint
    .deprecated()
    .summary("Get all switches of floor in build")
    .get
    .in(
      "builds" / path[String]("shortName") /
        "floors" / path[Int]("number") / "switches"
    )
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build or floor not found")
      )
    )
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            getSwitchesOf(shortName, number).mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val getUnplacedSwitchesOfBuildEndpoint = switchBaseEndpoint
    .summary("Get all unplaced switches in build")
    .get
    .in(
      "builds" / path[String]("shortName") /
        "switches" / "unplaced"
    )
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build not found")
      )
    )
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getUnplacedSwitchesOf(shortName).mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val getPlacedSwitchesOfFloorEndpoint = switchBaseEndpoint
    .summary("Get all placed switches of floor in build")
    .get
    .in(
      "builds" / path[String]("shortName") /
        "floors" / path[Int]("number") / "switches" / "placed"
    )
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build or floor not found")
      )
    )
    .out(jsonBody[List[SwitchResponse]])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            getPlacedSwitchesOf(shortName, number)
              .mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val getSwitchEndpoint = switchBaseEndpoint
    .summary("Get switch by name")
    .get
    .in("switches" / path[String]("name"))
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Switch not found")
      )
    )
    .out(jsonBody[SwitchResponse])
    .serverLogic { as => name =>
      as match {
        case AuthStatus.Succeed =>
          getSwitch(name).mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val addSwitchEndpoint = switchBaseEndpoint
    .summary("Add switch")
    .post
    .in("switches")
    .in(jsonBody[SwitchRequest])
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build or floor not found")
          .description(StatusCode.Conflict, "Switch already exists")
          .description(StatusCode.InternalServerError, "Something went wrong")
      )
    )
    .out(
      statusCode
        .description(StatusCode.Created, "Switch added")
        .and(jsonBody[SwitchResult])
    )
    .serverLogic { as => switch =>
      as match {
        case AuthStatus.Succeed =>
          createSwitch(switch)
            .map((StatusCode.Created, _))
            // TODO: add Conflict and InternalServerError status codes too.
            .mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val updateSwitchEndpoint = switchBaseEndpoint
    .summary("Update switch")
    .put
    .in("switches" / path[String]("name"))
    .in(jsonBody[SwitchRequest])
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Switch, build or floor not found")
          .description(StatusCode.InternalServerError, "Something went wrong")
      )
    )
    .out(jsonBody[SwitchResult])
    .serverLogic { as =>
      { case (name, switch) =>
        as match {
          case AuthStatus.Succeed =>
            updateSwitch(name, switch)
              // TODO: add InternalServerError status code too.
              .mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val updateSwitchPositionEndpoint = switchBaseEndpoint
    .summary("Update switch position")
    .patch
    .in("switches" / path[String]("name"))
    .in(jsonBody[SavePositionRequest])
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Switch not found")
      )
    )
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (name, position) =>
        as match {
          case AuthStatus.Succeed =>
            updateSwitchPosition(name, position)
              .mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val deleteSwitchEndpoint = switchBaseEndpoint
    .summary("Delete switch")
    .delete
    .in("switches" / path[String]("name"))
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Switch not found")
      )
    )
    .out(plainBody[Boolean])
    .serverLogic { as => name =>
      as match {
        case AuthStatus.Succeed =>
          (getSwitch(name) *> deleteSwitch(name))
            .mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val routes = List(
    runSyncEndpoint.widen[R],
    getSNMPCommunitiesEndpoint.widen[R],
    getSwitchesEndpoint.widen[R],
    getSwitchesOfBuildEndpoint.widen[R],
    getSwitchesOfFloorEndpoint.widen[R],
    getUnplacedSwitchesOfBuildEndpoint.widen[R],
    getPlacedSwitchesOfFloorEndpoint.widen[R],
    getSwitchEndpoint.widen[R],
    addSwitchEndpoint.widen[R],
    updateSwitchEndpoint.widen[R],
    updateSwitchPositionEndpoint.widen[R],
    deleteSwitchEndpoint.widen[R]
  )
}
