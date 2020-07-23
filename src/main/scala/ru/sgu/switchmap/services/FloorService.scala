package ru.sgu.switchmap.services

import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import ru.sgu.switchmap.models.{BuildNotFoundError, Floor, FloorNotFoundError}
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}

class FloorService(
  buildRepository: BuildRepository,
  floorRepository: FloorRepository,
  switchRepository: SwitchRepository
) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "builds" / shortName / "floors" =>
      Ok(
        Stream("[") ++ floorRepository
          .getFloorsOf(shortName)
          .map(createFloorResource(_).asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> Root / "builds" / shortName / "floors" / IntVar(number) =>
      for {
        result <- floorRepository.getFloorByBuildAndNumber(shortName, number)
        response <- floorResult(result)
      } yield response

    case req @ POST -> Root / "builds" / shortName / "floors" =>
      buildRepository.getBuildByShortName(shortName).flatMap {
        case Left(BuildNotFoundError) => NotFound()
        case Right(build) =>
          for {
            floor <- req.decodeJson[Floor]
            floor.buildName = build.name
            floor.buildShortName = build.shortName
            createdFloor <- floorRepository.createFloor(floor)
            response <- Created(
              createdFloor.asJson,
              Location(
                Uri.unsafeFromString(
                  s"/builds/${createdFloor.buildShortName}/floors/${createdFloor.number}"
                )
              )
            )
          } yield response
      }

    case DELETE -> Root / "builds" / shortName / "floors" / IntVar(number) =>
      floorRepository.deleteFloor(shortName, number).flatMap {
        case Left(FloorNotFoundError) => NotFound()
        case Right(_)                 => NoContent()
      }
  }

  private def floorResult(result: Either[FloorNotFoundError.type, Floor]) = {
    result match {
      case Left(FloorNotFoundError) => NotFound()
      case Right(floor)             => Ok(createFloorResource(floor).asJson.noSpaces)
    }
  }

  private def createFloorResource(f: Floor): IO[FloorResource] = {
    for {
      swNum <-
        switchRepository.getNumberOfSwitchesOfFloor(f.buildShortName, f.number)
    } yield FloorResource(f.number, swNum)
  }

}

case class FloorResource(
  number: Int,
  switchesNumber: Int
)
