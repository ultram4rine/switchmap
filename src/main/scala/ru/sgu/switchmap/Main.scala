package ru.sgu.switchmap

import cats.effect.{ExitCode => CatsExitCode}
import zio._
import zio.blocking.Blocking
import zio.console._
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.rho.swagger.{DefaultSwaggerFormats, SwaggerMetadata}
import com.http4s.rho.swagger.ui.SwaggerUi
import org.http4s.rho.swagger.models.{
  Info,
  Scheme,
  SecurityRequirement,
  ApiKeyAuthDefinition,
  In
}
import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel

import ru.sgu.switchmap.auth._
import ru.sgu.switchmap.config.Config
import ru.sgu.switchmap.routes._
import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.db.{FlywayMigrator, FlywayMigratorLive}
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}
import ru.sgu.switchmap.config.AppConfig
import ru.sgu.switchmap.config.LDAPConfig
import org.http4s.rho.swagger.SwaggerMetadata
import org.http4s.rho.swagger

object Main extends App {
  /* private val dsl = Http4sDsl[Task]
  import dsl._ */

  type HttpServerEnvironment = Clock with Blocking
  type AuthEnvironment = Has[Authenticator] with Has[Authorizer]
  type AppEnvironment = Config
    with AuthEnvironment
    with Has[FlywayMigrator]
    with HttpServerEnvironment
    with BuildRepository
    with FloorRepository
    with SwitchRepository

  val dbTransactor: TaskLayer[DBTransactor] =
    Config.live >>> DBTransactor.live

  val authEnvironment: TaskLayer[Has[Authenticator] with Has[Authorizer]] =
    Config.live >>> LDAPLive.layer ++ JWTLive.layer >>> AuthenticatorLive.layer ++ AuthorizerLive.layer
  val flywayMigrator: TaskLayer[Has[FlywayMigrator]] =
    Console.live ++ dbTransactor >>> FlywayMigratorLive.layer
  val httpServerEnvironment: ULayer[HttpServerEnvironment] =
    Clock.live ++ Blocking.live
  val buildRepository: TaskLayer[BuildRepository] =
    dbTransactor >>> BuildRepository.live
  val floorRepository: TaskLayer[FloorRepository] =
    dbTransactor >>> FloorRepository.live
  val switchRepository: TaskLayer[SwitchRepository] =
    dbTransactor >>> SwitchRepository.live
  val appEnvironment: TaskLayer[AppEnvironment] =
    Config.live ++ authEnvironment ++ flywayMigrator ++ httpServerEnvironment ++ buildRepository ++ floorRepository ++ switchRepository

  type AppTask[A] = RIO[AppEnvironment, A]
  object Auth extends org.http4s.rho.AuthedContext[AppTask, AuthInfo]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment with Console, Throwable, Unit] =
      for {
        api <- config.apiConfig
        app <- config.appConfig
        _ <- FlywayMigrator.migrate()

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

        httpApp = Router[AppTask](
          "/api/v2/auth" -> AuthRoutes().api.toRoutes(swaggerMiddleware),
          "/api/v2/" -> Middleware.middleware(
            Auth.toService(BuildRoutes().api.toRoutes(swaggerMiddleware))
          ),
          "/api/v2/" -> Middleware.middleware(
            Auth.toService(FloorRoutes().api.toRoutes(swaggerMiddleware))
          ),
          "/api/v2/" -> Middleware.middleware(
            Auth.toService(SwitchRoutes().api.toRoutes(swaggerMiddleware))
          )
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          //val ec = rts.platform.executor.asEC

          BlazeServerBuilder[AppTask]
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(httpApp)
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
