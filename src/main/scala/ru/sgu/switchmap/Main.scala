package ru.sgu.switchmap

import cats.syntax.semigroupk._
import cats.effect.{ExitCode => CatsExitCode}
import cats.data.Kleisli
import com.comcast.ip4s._
import com.http4s.rho.swagger.ui.SwaggerUi
import io.grpc.ManagedChannelBuilder
import org.http4s.Status.{Found, NotFound}
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.{HttpRoutes, HttpApp, Request, Response}
import org.http4s.rho.swagger.models._
import org.http4s.rho.swagger.{DefaultSwaggerFormats, SwaggerMetadata}
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import ru.sgu.git.netdataserv.netdataproto.ZioNetdataproto.NetDataClient
import ru.sgu.switchmap.auth._
import ru.sgu.switchmap.config.{Config, AppConfig}
import ru.sgu.switchmap.db.{DBTransactor, FlywayMigrator, FlywayMigratorLive}
import ru.sgu.switchmap.models.SwitchRequest
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}
import ru.sgu.switchmap.utils.{
  SeensUtil,
  SeensUtilLive,
  DNSUtil,
  DNSUtilLive,
  SNMPUtil,
  SNMPUtilLive
}
import ru.sgu.switchmap.routes._
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
import zio.logging.{Logging, log}
import zio.logging.slf4j.Slf4jLogger
import scala.io.Source

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

        _ <- repositories.sync()

        swaggerMiddleware = SwaggerUi[AppTask].createRhoMiddleware(
          swaggerFormats = DefaultSwaggerFormats,
          swaggerMetadata = SwaggerMetadata(
            apiInfo = Info(title = "SwitchMap API", version = "2.0.0"),
            host = Some(app.hostname),
            basePath = Some("/api/v2"),
            schemes = List(Scheme.HTTPS, Scheme.WSS),
            security = List(SecurityRequirement("JWT", List())),
            securityDefinitions = Map(
              "JWT" -> ApiKeyAuthDefinition(
                "X-Auth-Token",
                In.HEADER,
                Some("JWT")
              )
            )
          )
        )

        hub <- ZHub.unbounded[WebSocketFrame]

        httpAPI = (wsb: WebSocketBuilder2[AppTask]) =>
          Router[AppTask](
            "/api/v2" -> Middleware.middleware(
              AuthContext
                .toService(
                  AuthRoutes().api
                    .and(BuildRoutes().api)
                    .and(FloorRoutes().api)
                    .and(SwitchRoutes(wsb, hub).api)
                    .and(PlanRoutes().api)
                    .toRoutes(swaggerMiddleware)
                )
            )
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
