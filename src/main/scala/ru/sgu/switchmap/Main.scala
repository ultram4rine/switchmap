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
import io.grpc.ManagedChannelBuilder
import scalapb.zio_grpc.ZManagedChannel

//import ru.sgu.switchmap.auth.{LDAP, LDAPLive}
import ru.sgu.switchmap.config.Config
import ru.sgu.switchmap.routes._
import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.db.flywayMigrator.FlywayMigrator
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}

object Main extends App {
  private val dsl = Http4sDsl[Task]
  import dsl._

  type HttpServerEnvironment = Clock with Blocking
  type AppEnvironment = Config
  //with LDAP
    with FlywayMigrator
    with HttpServerEnvironment
    with BuildRepository
    with FloorRepository
    with SwitchRepository

  val dbTransactor: TaskLayer[DBTransactor] =
    Config.live >>> DBTransactor.live
  //val ldapEnvironment: TaskLayer[LDAP] = Config.live >>> LDAPLive.layer
  val flywayMigrator: TaskLayer[FlywayMigrator] =
    dbTransactor >>> FlywayMigrator.live
  val httpServerEnvironment: ULayer[HttpServerEnvironment] =
    Clock.live ++ Blocking.live
  val buildRepository: TaskLayer[BuildRepository] =
    dbTransactor >>> BuildRepository.live
  val floorRepository: TaskLayer[FloorRepository] =
    dbTransactor >>> FloorRepository.live
  val switchRepository: TaskLayer[SwitchRepository] =
    dbTransactor >>> SwitchRepository.live
  val appEnvironment: TaskLayer[AppEnvironment] =
    Config.live /* ++ ldapEnvironment */ ++ flywayMigrator ++ httpServerEnvironment ++ buildRepository ++ floorRepository ++ switchRepository

  type AppTask[A] = RIO[AppEnvironment, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment with Console, Throwable, Unit] =
      for {
        api <- config.apiConfig
        _ <- FlywayMigrator.migrate()
        httpApp = Router[AppTask](
          "/" -> BuildRoutes().route,
          "/" -> FloorRoutes().route,
          "/" -> SwitchRoutes().route
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
