package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder}
import ru.sgu.switchmap.auth.{Authorizer, AuthContext, AuthStatus}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.{BuildRequest, BuildResponse}
import ru.sgu.switchmap.repositories._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._

final case class BuildRoutes[R <: Has[Authorizer] with BuildRepository]() {
  val getBuildsEndpoint = secureEndpoint.get
    .in("builds")
    .out(jsonBody[List[BuildResponse]])
    .serverLogic { as => _ =>
      as match {
        case AuthStatus.Succeed => getBuilds().mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val getBuildEndpoint = secureEndpoint.get
    .in("builds" / path[String]("shortName"))
    .out(jsonBody[BuildResponse])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed => getBuild(shortName).mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val addBuildEndpoint = secureEndpoint.post
    .in("builds")
    .in(jsonBody[BuildRequest])
    .out(plainBody[Boolean])
    .serverLogic { as => build =>
      as match {
        case AuthStatus.Succeed => createBuild(build).mapError(_.toString())
        case _                  => ZIO.fail("401")
      }
    }
  val updateBuildEndpoint = secureEndpoint.put
    .in("builds" / path[String]("shortName"))
    .in(jsonBody[BuildRequest])
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, build) =>
        as match {
          case AuthStatus.Succeed =>
            updateBuild(shortName, build).mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }
  val deleteBuildEndpoint = secureEndpoint.delete
    .in("builds" / path[String]("shortName"))
    .out(plainBody[Boolean])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          (getBuild(shortName) *> deleteBuild(shortName)).mapError(_.toString())
        case _ => ZIO.fail("401")
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
