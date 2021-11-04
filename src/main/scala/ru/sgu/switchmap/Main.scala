package ru.sgu.switchmap

import cats.syntax.semigroupk._
import cats.effect.{ExitCode => CatsExitCode}
import cats.data.Kleisli
import com.http4s.rho.swagger.ui.SwaggerUi
import io.grpc.ManagedChannelBuilder
import org.http4s
import org.http4s.server.staticcontent.resourceServiceBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.{HttpRoutes, HttpApp, Request, Response}
import org.http4s.rho.swagger.models._
import org.http4s.rho.swagger.{DefaultSwaggerFormats, SwaggerMetadata}
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import ru.sgu.git.netdataserv.netdataproto.GetNetworkSwitchesRequest
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
import ru.sgu.switchmap.utils.seens.SeensUtil
import ru.sgu.switchmap.utils.dns.DNSUtil
import ru.sgu.switchmap.utils.snmp.SNMPUtil
import ru.sgu.switchmap.routes._
import scalapb.zio_grpc.ZManagedChannel
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._
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
  type AppEnvironment = Config
    with AuthEnvironment
    with Has[FlywayMigrator]
    with NetDataClient
    with HttpServerEnvironment
    with BuildRepository
    with FloorRepository
    with SwitchRepository

  val dbTransactor: TaskLayer[DBTransactor] =
    Config.live >>> DBTransactor.live

  val seensClient: TaskLayer[SeensUtil] = Config.live >>> SeensUtil.live
  val dnsEnvironment: TaskLayer[DNSUtil] = Config.live >>> DNSUtil.live
  val snmpEnvironment: TaskLayer[SNMPUtil] = Config.live >>> SNMPUtil.live

  val authEnvironment: TaskLayer[Has[Authenticator] with Has[Authorizer]] =
    Config.live >>> LDAPLive.layer ++ JWTLive.layer >>> AuthenticatorLive.layer ++ AuthorizerLive.layer
  val flywayMigrator: TaskLayer[Has[FlywayMigrator]] =
    Console.live ++ dbTransactor >>> FlywayMigratorLive.layer
  val netdataEnvironment: TaskLayer[NetDataClient] =
    Config.live >>> NetDataClientLive.layer
  val httpServerEnvironment: ULayer[HttpServerEnvironment] =
    Clock.live ++ Blocking.live
  val buildRepository: TaskLayer[BuildRepository] =
    dbTransactor >>> BuildRepository.live
  val floorRepository: TaskLayer[FloorRepository] =
    dbTransactor >>> FloorRepository.live
  val switchRepository: TaskLayer[SwitchRepository] =
    dbTransactor ++ Config.live ++ netdataEnvironment ++ seensClient ++ dnsEnvironment ++ snmpEnvironment >>> SwitchRepository.live
  val appEnvironment: TaskLayer[AppEnvironment] =
    Config.live ++ Console.live ++ authEnvironment ++ netdataEnvironment ++ flywayMigrator ++ httpServerEnvironment ++ buildRepository ++ floorRepository ++ switchRepository

  type AppTask[A] = RIO[AppEnvironment, A]

  def redirectToRootResponse(request: Request[AppTask]): Response[AppTask] = {
    if (!request.pathInfo.startsWithString("/api/v2")) {
      Response[AppTask]()
        .withStatus(http4s.Status.Found)
        .withEntity(
          Source
            .fromResource("public/index.html")
            .getLines()
            .mkString
        )
        .withHeaders(request.headers)
    } else {
      Response[AppTask]()
        .withStatus(http4s.Status.NotFound)
    }
  }

  def orRedirectToRoot(routes: HttpRoutes[AppTask]): HttpApp[AppTask] =
    Kleisli(req => routes.run(req).getOrElse(redirectToRootResponse(req)))

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment with Console, Object, Unit] =
      for {
        api <- config.apiConfig
        app <- config.appConfig
        _ <- FlywayMigrator.migrate()

        _ <- putStrLn("Retrieving switches")
        switches <- for {
          resp <-
            NetDataClient
              .getNetworkSwitches(GetNetworkSwitchesRequest())
        } yield resp.switch

        _ <- putStrLn("Adding switches to database")
        _ <- ZIO.foreach(switches)(sw =>
          repositories
            .createSwitch(
              SwitchRequest(
                true,
                true,
                true,
                sw.name,
                snmpCommunity = app.snmpCommunities.headOption.getOrElse("")
              )
            )
            .catchAll(e => {
              putStrLn(e.getMessage()) *> ZIO.succeed(false)
            })
        )
        _ <- putStrLn("Switches added")

        swaggerMiddleware = SwaggerUi[AppTask].createRhoMiddleware(
          swaggerFormats = DefaultSwaggerFormats,
          swaggerMetadata = SwaggerMetadata(
            apiInfo = Info(title = "SwitchMap API", version = "2.0.0-SNAPSHOT"),
            host = Some(app.hostname),
            basePath = Some("/api/v2"),
            schemes = List(Scheme.HTTPS),
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

        httpAPI = Router[AppTask](
          "/api/v2" -> Middleware.middleware(
            AuthContext
              .toService(
                AuthRoutes().api
                  .and(BuildRoutes().api)
                  .and(FloorRoutes().api)
                  .and(SwitchRoutes().api)
                  .and(PlanRoutes().api)
                  .toRoutes(swaggerMiddleware)
              )
          )
        )

        spa = Router[AppTask](
          "/" -> resourceServiceBuilder[AppTask]("/public").toRoutes
        )

        routes = orRedirectToRoot(spa <+> httpAPI)

        server <- ZIO.runtime[AppEnvironment].flatMap { _ =>
          //val ec = rts.platform.executor.asEC

          BlazeServerBuilder[AppTask]
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(
              CORS.policy.withAllowOriginAll
                .withAllowCredentials(false)
                .apply(routes)
            )
            .serve
            .compile[AppTask, AppTask, CatsExitCode]
            .drain
        }
      } yield server

    program
      .provideSomeLayer[ZEnv](appEnvironment)
      .tapError(err => putStrLn(s"Execution failed with: $err"))
      .exitCode
  }
}
