package ru.sgu.switchmap.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.{RIO, RLayer, ZIO}
import zio.logging.{console, LogContext}
import zio.logging.{log, Logger, Logging}

trait FlywayMigrator {
  def migrate(): RIO[LogContext, MigrateResult]
}

case class FlywayMigratorLive(
  logger: Logger[String],
  res: DBTransactor.Resource
) extends FlywayMigrator {
  override def migrate(): RIO[LogContext, MigrateResult] =
    for {
      _ <- log.info("Starting Flyway migration")
      res <- res.xa.configure(ds =>
        ZIO.attempt {
          Flyway.configure().dataSource(ds).load().migrate()
        }
      )
      _ <- log.info("Finished Flyway migration")
    } yield res
}

object FlywayMigratorLive {
  val layer: RLayer[Logger[String] with DBTransactor, FlywayMigrator] =
    (FlywayMigratorLive(_, _)).toLayer
}

object FlywayMigrator {
  def migrate(): RIO[FlywayMigrator with Logging, MigrateResult] =
    ZIO.environmentWithZIO[FlywayMigrator with Logging](_.get.migrate())
}
