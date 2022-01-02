package ru.sgu.switchmap.routes

import fs2.{Stream, Pipe}
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import io.circe.parser.decode
import org.http4s.headers
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.rho.bits._
import org.http4s.{EntityDecoder, EntityEncoder, Response, Challenge}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._
import ru.sgu.switchmap.auth.{AuthContext, Authorizer, AuthStatus, AuthToken}
import ru.sgu.switchmap.Main.AppTask
import ru.sgu.switchmap.models.{
  SwitchRequest,
  SwitchPosition,
  SavePositionRequest
}
import ru.sgu.switchmap.repositories._
import scodec.bits._
import shapeless.{::, HNil}
import zio._
import zio.interop.catz._
import zio.stream.ZStream
import zio.stream.interop.fs2z._
import zio.duration.durationInt

//import scala.concurrent.duration._

final case class SwitchRoutes[R <: Has[Authorizer] with SwitchRepository](
  wsb: WebSocketBuilder2[AppTask],
  hub: Hub[WebSocketFrame]
) {
  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AppTask, A] = jsonOf[AppTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AppTask, A] =
    jsonEncoderOf[AppTask, A]

  val api: RhoRoutes[AppTask] =
    new RhoRoutes[AppTask] {
      val swaggerIO: SwaggerSupport[AppTask] = SwaggerSupport[AppTask]
      import swaggerIO._

      import ru.sgu.switchmap.json._

      "Run switches sync with network" **
        GET / "switches" / "sync" >>> AuthContext.auth |>> {
          auth: AuthStatus.Status =>
            auth match {
              case AuthStatus.Succeed =>
                sync().foldM(_ => InternalServerError(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Get SNMP communitites" **
        GET / "switches" / "snmp" / "communities" >>> AuthContext.auth |>> {
          auth: AuthStatus.Status =>
            auth match {
              case AuthStatus.Succeed =>
                snmpCommunities().foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Get all switches" **
        GET / "switches" >>> AuthContext.auth |>> { auth: AuthStatus.Status =>
          auth match {
            case AuthStatus.Succeed =>
              getSwitches().foldM(_ => NotFound(()), Ok(_))
            case _ => Unauthorized(())
          }
        }

      "Get all switches of build" **
        GET / "builds" / pv"shortName" / "switches" >>> AuthContext.auth |>> {
          (shortName: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getSwitchesOf(shortName).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Get all unplaced switches in build" **
        GET / "builds" / pv"shortName" / "switches" / "unplaced" >>> AuthContext.auth |>> {
          (shortName: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getUnplacedSwitchesOf(shortName).foldM(
                  _ => NotFound(()),
                  Ok(_)
                )
              case _ => Unauthorized(())
            }
        }

      "Get all placed switches of floor in build" **
        GET / "builds" / pv"shortName" / "floors" / pathVar[Int](
          "number",
          "Number of floor"
        ) / "switches" >>> AuthContext.auth |>> {
          (shortName: String, number: Int, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getPlacedSwitchesOf(shortName, number).foldM(
                  _ => NotFound(()),
                  Ok(_)
                )
              case _ => Unauthorized(())
            }
        }

      "Connect to websocket" **
        GET / "builds" / pv"shortName" / "floors" / pathVar[Int](
          "number",
          "Number of floor"
        ) / "ws" >>> AuthContext.auth |>> {
          (
            shortName: String,
            number: Int,
            auth: AuthStatus.Status
          ) => mkWebSocket(shortName, number, auth)
        }

      "Get switch by name" **
        GET / "switches" / pv"name" >>> AuthContext.auth |>> {
          (name: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                getSwitch(name).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Add switch" **
        POST / "switches" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          SwitchRequest
        ] |>> { (auth: AuthStatus.Status, switch: SwitchRequest) =>
          auth match {
            case AuthStatus.Succeed =>
              createSwitch(switch).foldM(
                e => InternalServerError(e.getMessage),
                Created(_)
              )
            case _ => Unauthorized(())
          }
        }

      "Update switch" **
        PUT / "switches" / pv"name" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          SwitchRequest
        ] |>> {
          (name: String, auth: AuthStatus.Status, switch: SwitchRequest) =>
            auth match {
              case AuthStatus.Succeed =>
                updateSwitch(name, switch).foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }

      "Update switch position" **
        PATCH / "switches" / pv"name" >>> AuthContext.auth ^ jsonOf[
          AppTask,
          SavePositionRequest
        ] |>> {
          (
            name: String,
            auth: AuthStatus.Status,
            position: SavePositionRequest
          ) =>
            auth match {
              case AuthStatus.Succeed =>
                updateSwitchPosition(name, position).foldM(
                  _ => NotFound(()),
                  Ok(_)
                )
              case _ => Unauthorized(())
            }
        }

      "Delete switch" **
        DELETE / "switches" / pv"name" >>> AuthContext.auth |>> {
          (name: String, auth: AuthStatus.Status) =>
            auth match {
              case AuthStatus.Succeed =>
                (getSwitch(name) *> deleteSwitch(name))
                  .foldM(_ => NotFound(()), Ok(_))
              case _ => Unauthorized(())
            }
        }
    }

  val dsl: Http4sDsl[AppTask] = Http4sDsl[AppTask]
  import dsl._

  private def mkWebSocket(
    shortName: String,
    number: Int,
    auth: AuthStatus.Status
  ): AppTask[Response[AppTask]] = {
    auth match {
      case AuthStatus.Succeed => {
        val stream = ZStream.fromHub(hub).chunkN(1)
        val pingStream = ZStream
          .repeatWith(
            Ping(hex"70696e67"),
            Schedule.spaced(50.second)
          )
          .chunkN(1)
        def handle(s: Stream[AppTask, WebSocketFrame]): Stream[AppTask, Unit] =
          s.collect({
            case Text(text, _) => {
              decode[SwitchPosition](text)
                .fold(_ => (), _ => ())
              Text(text)
            }
          }).evalMap(hub.publish(_).map(_ => ()))

        val toClient: Stream[AppTask, WebSocketFrame] =
          ZStream.mergeAll(2)(stream, pingStream).toFs2Stream
        val fromClient: Pipe[AppTask, WebSocketFrame, Unit] = handle

        wsb.build(toClient, fromClient)
      }
      case _ =>
        Unauthorized(
          `WWW-Authenticate`(
            Challenge(
              "X-Auth-Token",
              "SwitchMap",
              Map.empty
            )
          )
        )
    }
  }
}
