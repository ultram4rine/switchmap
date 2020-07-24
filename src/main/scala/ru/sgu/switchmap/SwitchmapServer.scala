package ru.sgu.switchmap

import cats.effect.{
  Blocker,
  ConcurrentEffect,
  ContextShift,
  ExitCode,
  IO,
  Resource,
  Timer
}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import ru.sgu.switchmap.config.Config.Config
import ru.sgu.switchmap.db.Database
import ru.sgu.switchmap.repositories.{
  BuildRepository,
  FloorRepository,
  SwitchRepository
}
import ru.sgu.switchmap.services.{BuildService, FloorService, SwitchService}

object SwitchmapServer {

  def create(configFile: String = "application.conf")(implicit
    contextShift: ContextShift[IO],
    concurrentEffect: ConcurrentEffect[IO],
    timer: Timer[IO]
  ): IO[ExitCode] = {
    resources(configFile).use(create)
  }

  private def resources(
    configFile: String
  )(implicit contextShift: ContextShift[IO]): Resource[IO, Resources] = {
    for {
      config <- Config.load(configFile)
      ec <-
        ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      blocker <- Blocker[IO]
      transactor <- Database.transactor(config.database, ec, blocker)
    } yield Resources(transactor, config)
  }

  private def create(resources: Resources)(implicit
    concurrentEffect: ConcurrentEffect[IO],
    timer: Timer[IO]
  ): IO[ExitCode] = {
    for {
      _ <- Database.initialize(resources.transactor)
      buildRepository = new BuildRepository(resources.transactor)
      floorRepository = new FloorRepository(resources.transactor)
      switchRepository = new SwitchRepository(resources.transactor)
      exitCode <-
        BlazeServerBuilder[IO]
          .bindHttp(resources.config.server.port, resources.config.server.host)
          .withHttpApp(
            (new BuildService(
              buildRepository,
              floorRepository,
              switchRepository
            ).routes <+> new FloorService(
              buildRepository,
              floorRepository,
              switchRepository
            ).routes <+> new SwitchService(switchRepository).routes).orNotFound
          )
          .serve
          .compile
          .lastOrError
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)

}
