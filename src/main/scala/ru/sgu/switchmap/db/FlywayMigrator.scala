package ru.sgu.switchmap.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.console.{Console, putStrLn}
import zio.{Has, RIO, RLayer, ZIO}

trait FlywayMigrator {
  def migrate(): RIO[Console, MigrateResult]
}

case class FlywayMigratorLive(
  console: Console.Service,
  res: DBTransactor.Resource
) extends FlywayMigrator {
  override def migrate(): RIO[Console, MigrateResult] =
    for {
      _ <- putStrLn("Starting Flyway migration")
      res <- res.xa.configure(ds =>
        ZIO.effect {
          Flyway.configure().dataSource(ds).load().migrate()
        }
      )
      _ <- putStrLn("Finished Flyway migration")
    } yield res
}

object FlywayMigratorLive {
  val layer: RLayer[Has[Console.Service] with Has[DBTransactor.Resource], Has[
    FlywayMigrator
  ]] =
    (FlywayMigratorLive(_, _)).toLayer
}

object FlywayMigrator {
  def migrate(): RIO[Has[FlywayMigrator] with Console, MigrateResult] =
    ZIO.accessM[Has[FlywayMigrator] with Console](_.get.migrate())
}
