package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import ru.sgu.switchmap.auth.Authorizer
import ru.sgu.switchmap.models.SwitchPosition
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}
import scala.concurrent.duration._
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.CodecFormat
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import sttp.ws.WebSocketFrame
import zio._
import zio.stream.Stream

final case class WebSocketRoutes[R <: Has[
  Authorizer
] with BuildRepository with FloorRepository with SwitchRepository](
  hub: Hub[SwitchPosition]
) {
  private[this] val baseEndpoint = secureCookieEndpoint.tag("websocket")

  val websocketReceiveEndpoint = endpoint
    .summary("Connect to websocket to send data to it")
    .in(
      "builds" / path[String]("shortName") /
        "floors" / path[Int]("number") / "ws" / "send"
    )
    .out(
      webSocketBody[
        SwitchPosition,
        CodecFormat.Json,
        SwitchPosition,
        CodecFormat.Json
      ](ZioStreams).autoPing(
        Some((50.seconds, WebSocketFrame.ping))
      )
    )
    .zServerLogic { // as =>
      { case (shortName, number) =>
        /* as match {
          case AuthStatus.Succeed => { */
        ZIO.succeed(_.tap(hub.publish(_)))
      }
      /* case _ => ZIO.fail(())
        }
      }*/
    }

  val websocketSendEndpoint = endpoint
    .summary("Connect to websocket to receive data from it")
    .in(
      "builds" / path[String]("shortName") /
        "floors" / path[Int]("number") / "ws" / "receive"
    )
    .out(
      webSocketBody[
        SwitchPosition,
        CodecFormat.Json,
        SwitchPosition,
        CodecFormat.Json
      ](ZioStreams).autoPing(
        Some((50.seconds, WebSocketFrame.ping))
      )
    )
    .zServerLogic { // as =>
      { case (shortName, number) =>
        /* as match {
          case AuthStatus.Succeed => { */
        ZIO
          .succeed(_ => Stream.fromHub(hub))
      /* }
          case _ => ZIO.fail(())
        } */
      }
    }

  val routes = List(
    websocketReceiveEndpoint.widen[R],
    websocketSendEndpoint.widen[R]
  )
}
