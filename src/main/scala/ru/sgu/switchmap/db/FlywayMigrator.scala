package ru.sgu.switchmap.db

import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.console.{Console, putStrLn}
import zio.{ZIO, Task, Has, URLayer, ZLayer}

import ru.sgu.switchmap.config.DBConfig

object flywayMigrator {
  type FlywayMigrator = Has[FlywayMigrator.Service]

  object FlywayMigrator {
    trait Service {
      def migrate(): ZIO[Console, Throwable, Unit]
    }

    val live: URLayer[DBTransactor, FlywayMigrator] =
      ZLayer.fromService { res =>
        new Service {
          override def migrate(): ZIO[Console, Throwable, Unit] =
            for {
              _ <- putStrLn("Starting Flyway migration")
              _ <- res.xa.configure(ds =>
                ZIO.effect {
                  Flyway.configure().dataSource(ds).load().migrate()
                }
              )
              _ <- putStrLn("Finished Flyway migration")
            } yield ()
        }
      }

    def migrate(): ZIO[FlywayMigrator with Console, Throwable, Unit] =
      ZIO.accessM[FlywayMigrator with Console](
        _.get.migrate()
      )
  }
}
