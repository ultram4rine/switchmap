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
import ru.sgu.switchmap.models.{FloorRequest, FloorResponse}
import ru.sgu.switchmap.repositories._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._

final case class FloorRoutes[R <: Has[Authorizer] with FloorRepository]() {
  val getFloorsOfEndpoint = secureEndpoint.get
    .in("builds" / path[String]("shortName") / "floors")
    .out(jsonBody[List[FloorResponse]])
    .serverLogic { as => shortName =>
      as match {
        case AuthStatus.Succeed =>
          getFloorsOf(shortName).mapError(_.toString())
        case _ => ZIO.fail("401")
      }
    }
  val getFloorEndpoint = secureEndpoint.get
    .in("builds" / path[String]("shortName") / "floors" / path[Int]("number"))
    .out(jsonBody[FloorResponse])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            getFloor(shortName, number).mapError(_.toString())
          case _ => ZIO.fail("401")
        }
      }
    }
  val addFloorEndpoint = secureEndpoint.post
    .in("builds" / path[String]("shortName"))
    .in(jsonBody[FloorRequest])
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, floor) =>
        as match {
          case AuthStatus.Succeed => createFloor(floor).mapError(_.toString())
          case _                  => ZIO.fail("401")
        }
      }
    }
  val deleteFloorEndpoint = secureEndpoint.delete
    .in("builds" / path[String]("shortName") / "floors" / path[Int]("number"))
    .out(plainBody[Boolean])
    .serverLogic { as =>
      { case (shortName, number) =>
        as match {
          case AuthStatus.Succeed =>
            (getFloor(shortName, number) *> deleteFloor(shortName, number))
              .mapError(_.toString())
          case _ => ZIO.fail("401")
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
