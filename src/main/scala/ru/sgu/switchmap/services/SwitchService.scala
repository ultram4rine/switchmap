package ru.sgu.switchmap.services

import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import ru.sgu.switchmap.models.{Switch, SwitchNotFoundError}
import ru.sgu.switchmap.repositories.SwitchRepository

class SwitchService(
  switchRepository: SwitchRepository
) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "switches" =>
      Ok(
        Stream("[") ++ switchRepository.getSwitches
          .map(_.asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> Root / "builds" / shortName / "switches" =>
      Ok(
        Stream("[") ++ switchRepository
          .getSwitchesOfBuild(shortName)
          .map(_.asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> Root / "builds" / shortName / "floors" / IntVar(
          number
        ) / "switches" =>
      Ok(
        Stream("[") ++ switchRepository
          .getSwitchesOfFloor(shortName, number)
          .map(_.asJson.noSpaces)
          .intersperse(",") ++ Stream("]"),
        `Content-Type`(MediaType.application.json)
      )

    case GET -> Root / "switches" / name =>
      for {
        result <- switchRepository.getSwitchByName(name)
        response <- switchResult(result)
      } yield response

    case req @ POST -> Root / "switches" =>
      for {
        switch <- req.decodeJson[Switch]
        createdSwitch <- switchRepository.createSwitch(switch)
        response <- Created(
          createdSwitch.asJson,
          Location(
            Uri.unsafeFromString(s"/switches/${createdSwitch.name}")
          )
        )
      } yield response

    case req @ PUT -> Root / "switches" / name =>
      for {
        switch <- req.decodeJson[Switch]
        updateResult <- switchRepository.updateSwitch(name, switch)
        response <- switchResult(updateResult)
      } yield response

    case DELETE -> Root / "switches" / name =>
      switchRepository.deleteSwitch(name).flatMap {
        case Left(SwitchNotFoundError) => NotFound()
        case Right(_)                  => NoContent()
      }
  }

  private def switchResult(result: Either[SwitchNotFoundError.type, Switch]) = {
    result match {
      case Left(SwitchNotFoundError) => NotFound()
      case Right(switch)             => Ok(switch.asJson.noSpaces)
    }
  }

}
