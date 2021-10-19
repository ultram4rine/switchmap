package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, AuthedRoutes}
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.auth.{AuthInfo, Authorizer}
import ru.sgu.switchmap.models.Switch
import ru.sgu.switchmap.repositories._

final case class SwitchRoutes[R <: Has[Authorizer] with SwitchRepository]() {

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

  def route: AuthedRoutes[AuthInfo, SwitchTask] = {
    AuthedRoutes.of[AuthInfo, SwitchTask] {
      case GET -> Root / "switches" as authToken =>
        getSwitches().foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName / "switches" as authToken =>
        getSwitchesOf(shortName).foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "builds" / shortName / "floors" / IntVar(
            number
          ) / "switches" as authToken =>
        getSwitchesOf(shortName, number).foldM(_ => NotFound(), Ok(_))

      case GET -> Root / "switches" / name as authToken =>
        getSwitch(name).foldM(_ => NotFound(), Ok(_))

      case req @ POST -> Root / "switches" as authToken =>
        req.req.decode[Switch] { switch =>
          Created(createSwitch(switch))
        }

      case req @ PUT -> Root / "switches" / name as authToken =>
        req.req.decode[Switch] { switch =>
          updateSwitch(name, switch).foldM(_ => NotFound(), Ok(_))
        }

      case DELETE -> Root / "switches" / name as authToken =>
        (getSwitch(name) *> deleteSwitch(name))
          .foldM(_ => NotFound(), Ok(_))
    }
  }
}
