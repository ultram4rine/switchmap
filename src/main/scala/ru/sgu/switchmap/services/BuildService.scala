package ru.sgu.switchmap.services

import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import ru.sgu.switchmap.models.{Build, BuildNotFoundError}
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}

class BuildService(
  buildRepository: BuildRepository,
  floorRepository: FloorRepository,
  switchRepository: SwitchRepository
) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "builds" =>
      Ok(
        Stream("[") ++ buildRepository.getBuilds
          .map(createBuildResource(_).asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> Root / "builds" / shortName =>
      for {
        result <- buildRepository.getBuildByShortName(shortName)
        response <- buildResult(result)
      } yield response

    case req @ POST -> Root / "builds" =>
      for {
        build <- req.decodeJson[Build]
        createdBuild <- buildRepository.createBuild(build)
        response <- Created(
          createdBuild.asJson,
          Location(
            Uri.unsafeFromString(s"/builds/${createdBuild.shortName}")
          )
        )
      } yield response

    case req @ PUT -> Root / "builds" / shortName =>
      for {
        build <- req.decodeJson[Build]
        updateResult <- buildRepository.updateBuild(shortName, build)
        response <- buildResult(updateResult)
      } yield response

    case DELETE -> Root / "builds" / shortName =>
      buildRepository.deleteBuild(shortName).flatMap {
        case Left(BuildNotFoundError) => NotFound()
        case Right(_)                 => NoContent()
      }
  }

  private def buildResult(result: Either[BuildNotFoundError.type, Build]) = {
    result match {
      case Left(BuildNotFoundError) => NotFound()
      case Right(build)             => Ok(createBuildResource(build).asJson.noSpaces)
    }
  }

  private def createBuildResource(b: Build): IO[BuildResource] = {
    for {
      fNum <- floorRepository.getNumberOfFloorsOf(b.shortName)
      swNum <- switchRepository.getNumberOfSwitchesOfBuild(b.shortName)
    } yield BuildResource(b.name, b.shortName, fNum, swNum)
  }

}

case class BuildResource(
  name: String,
  shortName: String,
  floorsNumber: Int,
  switchesNumber: Int
)
