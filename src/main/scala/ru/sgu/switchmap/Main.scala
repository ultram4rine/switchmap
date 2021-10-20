package ru.sgu.switchmap

import cats.effect.{ExitCode => CatsExitCode}
import com.http4s.rho.swagger.ui.SwaggerUi
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.rho.swagger.models._
import org.http4s.rho.swagger.{DefaultSwaggerFormats, SwaggerMetadata}
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import ru.sgu.switchmap.auth._
import ru.sgu.switchmap.config.Config
import ru.sgu.switchmap.db.{DBTransactor, FlywayMigrator, FlywayMigratorLive}
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}
import ru.sgu.switchmap.routes._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.interop.catz._

object Main extends App {
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
    Config.live ++ Console.live ++ authEnvironment ++ flywayMigrator ++ httpServerEnvironment ++ buildRepository ++ floorRepository ++ switchRepository

  type AppTask[A] = RIO[AppEnvironment, A]

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
          "/api/v2" -> Middleware.middleware(
            AuthContext.toService(
              AuthRoutes().api
                .and(BuildRoutes().api)
                .and(FloorRoutes().api)
                .and(SwitchRoutes().api)
                .toRoutes(swaggerMiddleware)
            )
          )
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          //val ec = rts.platform.executor.asEC

          BlazeServerBuilder[AppTask]
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(
              CORS.policy.withAllowOriginAll
                .withAllowCredentials(false)
                .apply(httpApp)
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
