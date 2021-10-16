package ru.sgu.switchmap.db

import doobie.Transactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.console.{Console, putStrLn}
import zio.{ZIO, Has, URLayer, ZLayer, ZManaged}

import ru.sgu.switchmap.config.DBConfig

object FlywayMigrator {
  def migrate(cfg: DBConfig): ZIO[Any, Throwable, MigrateResult] = {
    val resource: ZManaged[Any, Throwable, MigrateResult] = ZIO.effect {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
        .migrate()
    }.toManaged_
    resource.useNow
  }
}
