package ru.sgu.switchmap.db

import doobie.Transactor
import org.flywaydb.core.Flyway
import zio.console.{Console, putStrLn}
import zio.{ZIO, Has, URLayer, ZLayer}

import ru.sgu.switchmap.config.DBConfig

object flywayMigrator {
  type FlywayMigrator = Has[FlywayMigrator.Service]

  object FlywayMigrator {
    trait Service {
      def migrate(): ZIO[Console, Throwable, Unit]
    }

    val live: URLayer[Has[DBConfig], FlywayMigrator] =
      ZLayer.fromService { cfg =>
        new Service {
          override def migrate(): ZIO[Console, Throwable, Unit] =
            for {
              _ <- putStrLn("Starting Flyway migration")
              _ <- ZIO.effect {
                Flyway
                  .configure()
                  .dataSource(cfg.url, cfg.user, cfg.password)
                  .load()
                  .migrate()
              }
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
