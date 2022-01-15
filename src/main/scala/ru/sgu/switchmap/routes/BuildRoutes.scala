package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import ru.sgu.switchmap.auth.{Authorizer, AuthStatus}
import ru.sgu.switchmap.models.{BuildRequest, BuildResponse}
import ru.sgu.switchmap.repositories._
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio._

final case class BuildRoutes[R <: Has[Authorizer] with BuildRepository]() {
  private[this] val buildBaseEndpoint = secureEndpoint.tag("builds")

  val getBuildsEndpoint = buildBaseEndpoint
    .summary("Get all builds")
    .get
    .in("builds")
    .out(jsonBody[List[BuildResponse]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => getBuilds().mapError(_.toString())
        case _                  => ZIO.fail(())
      }
    }

  val getBuildEndpoint = buildBaseEndpoint
    .summary("Get build by short name")
    .get
    .in("builds" / path[String]("shortName"))
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode.description(StatusCode.NotFound, "Build not found")
      )
    )
    .out(jsonBody[BuildResponse])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getBuild(shortName)
            .mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val addBuildEndpoint = buildBaseEndpoint
    .summary("Add build")
    .post
    .in("builds")
    .in(jsonBody[BuildRequest])
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode.description(StatusCode.Conflict, "Build already exists")
      )
    )
    .out(
      statusCode
        .description(StatusCode.Created, "Build successfully added")
        .and(plainBody[Boolean])
    )
    .serverLogic { as => build =>
      as match {
        case AuthStatus.Succeed =>
          createBuild(build)
            .map((StatusCode.Created, _))
            .mapError(_ => StatusCode.Conflict)
        case _ => ZIO.fail(())
      }
    }

  val updateBuildEndpoint = buildBaseEndpoint
    .summary("Update build")
    .put
    .in("builds" / path[String]("shortName"))
    .in(jsonBody[BuildRequest])
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode.description(StatusCode.NotFound, "Build not found")
      )
    )
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, build) =>
        as match {
          case AuthStatus.Succeed =>
            updateBuild(shortName, build)
              .mapError(_ => StatusCode.NotFound)
          case _ => ZIO.fail(())
        }
      }
    }

  val deleteBuildEndpoint = buildBaseEndpoint
    .summary("Delete build")
    .delete
    .in("builds" / path[String]("shortName"))
    .errorOutVariant(
      oneOfVariantFromMatchType(
        statusCode.description(StatusCode.NotFound, "Build not found")
      )
    )
    .out(plainBody[Boolean])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          (getBuild(shortName) *> deleteBuild(shortName))
            .mapError(_ => StatusCode.NotFound)
        case _ => ZIO.fail(())
      }
    }

  val routes = List(
    getBuildsEndpoint.widen[R],
    getBuildEndpoint.widen[R],
    addBuildEndpoint.widen[R],
    updateBuildEndpoint.widen[R],
    deleteBuildEndpoint.widen[R]
  )
}
