package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import ru.sgu.switchmap.auth.{Authorizer, AuthStatus}
import ru.sgu.switchmap.models.{FloorRequest, FloorResponse}
import ru.sgu.switchmap.repositories._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio._

final case class FloorRoutes[R <: Has[Authorizer] with FloorRepository]() {
  private[this] val floorBaseEndpoint = secureEndpoint.tag("floors")

  val getFloorsOfEndpoint = floorBaseEndpoint
    .summary("Get all floors of build")
    .get
    .in("builds" / path[String]("shortName") / "floors")
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode.description(StatusCode.NotFound, "Build not found")
      )
    )
    .out(jsonBody[List[FloorResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getFloorsOf(shortName).mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val getFloorEndpoint = floorBaseEndpoint
    .summary("Get floor of build by number")
    .get
    .in("builds" / path[String]("shortName") / "floors" / path[Int]("number"))
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode.description(StatusCode.NotFound, "Build or floor not found")
      )
    )
    .out(jsonBody[FloorResponse])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            getFloor(shortName, number).mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val addFloorEndpoint = floorBaseEndpoint
    .summary("Add floor to build")
    .post
    .in("builds" / path[String]("shortName"))
    .in(jsonBody[FloorRequest])
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build not found")
          .description(StatusCode.Conflict, "Floor already exists")
      )
    )
    .out(
      statusCode
        .description(StatusCode.Created, "Floor successfully added")
        .and(plainBody[Boolean])
    )
    .serverLogic { as =>
      { case (shortName, floor) =>
        as match {
          case AuthStatus.Succeed =>
            createFloor(floor)
              .map((StatusCode.Created, _))
              .mapError { case e: java.sql.SQLException =>
                if (
                  e.getSQLState == doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION.value
                ) {
                  StatusCode.NotFound
                } else if (
                  e.getSQLState == doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION.value
                ) {
                  StatusCode.Conflict
                }
              }
          case _ => ZIO.fail(())
        }
      }
    }

  val deleteFloorEndpoint = floorBaseEndpoint
    .summary("Delete floor of build by number")
    .delete
    .in("builds" / path[String]("shortName") / "floors" / path[Int]("number"))
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode
          .description(StatusCode.NotFound, "Build or floor not found")
      )
    )
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            (getFloor(shortName, number) *> deleteFloor(shortName, number))
              .mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val routes = List(
    getFloorsOfEndpoint.widen[R],
    getFloorEndpoint.widen[R],
    addFloorEndpoint.widen[R],
    deleteFloorEndpoint.widen[R]
  )
}
