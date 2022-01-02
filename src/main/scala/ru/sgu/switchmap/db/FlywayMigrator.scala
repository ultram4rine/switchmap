package ru.sgu.switchmap.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.logging.{Logging, Logger, log}
import zio.{Has, RIO, RLayer, ZIO}

trait FlywayMigrator {
  def migrate(): RIO[Logging, MigrateResult]
}

case class FlywayMigratorLive(
  logger: Logger[String],
  res: DBTransactor.Resource
) extends FlywayMigrator {
  override def migrate(): RIO[Logging, MigrateResult] =
    for {
      _ <- log.info("Starting Flyway migration")
      res <- res.xa.configure(ds =>
        ZIO.effect {
          Flyway.configure().dataSource(ds).load().migrate()
        }
      )
      _ <- log.info("Finished Flyway migration")
    } yield res
}

object FlywayMigratorLive {
  val layer: RLayer[Has[Logger[String]] with DBTransactor, Has[
    FlywayMigrator
  ]] =
    (FlywayMigratorLive(_, _)).toLayer
}

object FlywayMigrator {
  def migrate(): RIO[Has[FlywayMigrator] with Logging, MigrateResult] =
    ZIO.accessM[Has[FlywayMigrator] with Logging](_.get.migrate())
}
