package ru.sgu.switchmap

import cats.data.Kleisli
import cats.syntax.semigroupk._
import io.grpc.ManagedChannelBuilder
import org.http4s.{HttpApp, HttpRoutes, Request, Response}
import org.http4s.Status.{Found, NotFound}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import ru.sgu.git.netdataserv.netdataproto.ZioNetdataproto.NetDataClient
import ru.sgu.switchmap.auth._
import ru.sgu.switchmap.config.{AppConfig, Config}
import ru.sgu.switchmap.db.{DBTransactor, FlywayMigrator, FlywayMigratorLive}
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}
import ru.sgu.switchmap.routes._
import ru.sgu.switchmap.utils.{DNSUtilLive, SeensUtilLive, SNMPUtilLive}
import scala.io.Source
import scalapb.zio_grpc.ZManagedChannel
import sttp.tapir.openapi
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.logging.{log, Logging}
import zio.logging.slf4j.Slf4jLogger

private object NetDataClientLive {
  val layer: RLayer[Has[AppConfig], NetDataClient] =
    ZLayer.fromServiceManaged { cfg =>
      NetDataClient.managed(
        ZManagedChannel(
          ManagedChannelBuilder
            .forAddress(cfg.netdataHost, cfg.netdataPort)
            .usePlaintext()
        )
      )
    }
}

object Main extends App {
  type HttpServerEnvironment = Clock with Blocking
  type AuthEnvironment = Has[Authenticator] with Has[Authorizer]
  type AppEnvironment = Logging
    with Config
    with Has[FlywayMigrator]
    with Has[LDAP]
    with AuthEnvironment
    with NetDataClient
    with HttpServerEnvironment
    with BuildRepository
    with FloorRepository
    with SwitchRepository

  val logLayer: ULayer[Logging] = Slf4jLogger.make((_, msg) => msg)

  val dbTransactor: TaskLayer[DBTransactor] =
    Config.live >>> DBTransactor.live
  val flywayMigrator: TaskLayer[Has[FlywayMigrator]] =
    logLayer ++ dbTransactor >>> FlywayMigratorLive.layer

  val ldapEnvironment: TaskLayer[Has[LDAP]] =
    Config.live >>> LDAPLive.layer
  val authEnvironment: TaskLayer[Has[Authenticator] with Has[Authorizer]] =
    Config.live >>> LDAPLive.layer ++ JWTLive.layer >>> AuthenticatorLive.layer ++ AuthorizerLive.layer

  val netdataEnvironment: TaskLayer[NetDataClient] =
    Config.live >>> NetDataClientLive.layer
  val httpServerEnvironment: ULayer[HttpServerEnvironment] =
    Clock.live ++ Blocking.live

  val buildRepository: TaskLayer[BuildRepository] =
    dbTransactor >>> BuildRepository.live
  val floorRepository: TaskLayer[FloorRepository] =
    dbTransactor >>> FloorRepository.live
  val switchRepository: TaskLayer[SwitchRepository] =
    logLayer ++ dbTransactor ++ Config.live >+>
      netdataEnvironment ++
      SeensUtilLive.layer ++
      DNSUtilLive.layer ++
      SNMPUtilLive.layer >>>
      SwitchRepository.live

  val appEnvironment: TaskLayer[AppEnvironment] =
    logLayer ++ Config.live ++ flywayMigrator ++ ldapEnvironment ++ authEnvironment ++ netdataEnvironment ++ httpServerEnvironment ++ buildRepository ++ floorRepository ++ switchRepository

  type AppTask[A] = RIO[AppEnvironment, A]

  def redirectToRootResponse(request: Request[AppTask]): Response[AppTask] = {
    if (!request.pathInfo.startsWithString("/api/v2")) {
      Response[AppTask]()
        .withStatus(Found)
        .withEntity(
          Source
            .fromResource("public/index.html")
            .getLines()
            .mkString
        )
        .withHeaders(request.headers)
    } else {
      Response[AppTask]()
        .withStatus(NotFound)
    }
  }

  def orRedirectToRoot(routes: HttpRoutes[AppTask]): HttpApp[AppTask] =
    Kleisli(req => routes.run(req).getOrElse(redirectToRootResponse(req)))

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: RIO[AppEnvironment with Console, Unit] =
      for {
        api <- config.apiConfig
        app <- config.appConfig

        _ <- FlywayMigrator.migrate()

        _ <- LDAP.conn

        _ <- log.info("Retrieving switches")
        _ <- repositories.sync()
        _ <- log.info("Switches added")

        endpoints = List.concat(
          AuthRoutes[AppEnvironment]().routes,
          BuildRoutes[AppEnvironment]().routes,
          FloorRoutes[AppEnvironment]().routes,
          SwitchRoutes[AppEnvironment]().routes,
          PlanRoutes[AppEnvironment]().routes
        )

        swaggerEndpoints = SwaggerInterpreter(
          contextPath = List("api", "v2"),
          addServerWhenContextPathPresent = true
        )
          .fromServerEndpoints[AppTask](
            endpoints,
            openapi.Info(
              title = "SwitchMap API",
              version = "2.0.0",
              description = Some("Definition of SwitchMap API"),
              license = Some(
                openapi.License(
                  "Apache-2.0",
                  Some(
                    "https://git.sgu.ru/ultramarine/switchmap/blob/master/LICENSE"
                  )
                )
              )
            )
          )

        httpAPI = (wsb: WebSocketBuilder2[AppTask]) =>
          Router[AppTask](
            "/api/v2" -> ZHttp4sServerInterpreter()
              .from(endpoints ::: swaggerEndpoints)
              .toRoutes
          )

        spa = Router[AppTask](
          "/" -> resourceServiceBuilder[AppTask]("/public").toRoutes
        )

        routes = (wsb: WebSocketBuilder2[AppTask]) =>
          orRedirectToRoot(spa <+> httpAPI(wsb))

        server <- EmberServerBuilder
          .default[AppTask]
          .withHost(api.endpoint)
          .withPort(api.port)
          .withHttpWebSocketApp { wsb =>
            CORS.policy.withAllowOriginAll
              .withAllowCredentials(false)
              .apply(routes(wsb))
          }
          .build
          .toManagedZIO
          .use(server =>
            Task
              .succeed(
                putStrLn(s"Server Has Started at ${server.address}")
              ) *> UIO.never.as(())
          )
      } yield server

    program
      .provideSomeLayer[ZEnv](appEnvironment)
      .tapError(err => putStrLn(s"Execution failed with: $err"))
      .exitCode
  }
}
