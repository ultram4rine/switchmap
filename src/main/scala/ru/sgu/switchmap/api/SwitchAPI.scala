package ru.sgu.switchmap.api

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.models._
import ru.sgu.switchmap.repositories._

final case class SwitchAPI[R <: SwitchRepository]() {

  type SwitchTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[SwitchTask, A] = jsonOf[SwitchTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[SwitchTask, A] =
    jsonEncoderOf[SwitchTask, A]

  val dsl: Http4sDsl[SwitchTask] = Http4sDsl[SwitchTask]
  import dsl._

  def route: HttpRoutes[SwitchTask] = {
    HttpRoutes.of[SwitchTask] {
      case GET -> Root / "switches" =>
        getSwitches().foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName / "switches" =>
        getSwitchesOf(shortName).foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName / "floors" / IntVar(
            number
          ) / "switches" =>
        getSwitchesOf(shortName, number).foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "switches" / name =>
        getSwitch(name).foldM(_ => NotFound(), Ok(_))

      case req @ POST -> Root / "switches" =>
        req.decode[Switch] { switch =>
          Created(createSwitch(switch))
        }

      case req @ PUT -> Root / "switches" / name =>
        req.decode[Switch] { switch =>
          updateSwitch(name, switch).foldM(_ => NotFound(), Ok(_))
        }

      case DELETE -> Root / "switches" / name =>
        (getSwitch(name) *> deleteSwitch(name))
          .foldM(_ => NotFound(), Ok(_))
    }
  }
}
