package ru.sgu.switchmap

import doobie.hikari.HikariTransactor
import ru.sgu.switchmap.config.DBConfig
import zio.interop.catz._
import zio.{Has, Task, ZIO, ZLayer}

package object db {
  type DBTransactor = Has[DBTransactor.Resource]

  object DBTransactor {
    private implicit val zioRuntime: zio.Runtime[zio.ZEnv] =
      zio.Runtime.default

    private implicit val dispatcher: cats.effect.std.Dispatcher[zio.Task] =
      zioRuntime
        .unsafeRun(
          cats.effect.std
            .Dispatcher[zio.Task]
            .allocated
        )
        ._1

    trait Resource {
      val xa: HikariTransactor[Task]
    }

    val live: ZLayer[Has[DBConfig], Throwable, DBTransactor] =
      ZLayer.fromServiceManaged { cfg =>
        for {
          rt <- ZIO.runtime[Any].toManaged_
          hxa <-
            HikariTransactor
              .newHikariTransactor[Task](
                "org.postgresql.Driver",
                cfg.url,
                cfg.user,
                cfg.password,
                rt.platform.executor.asEC
              )
              .toManaged
        } yield new Resource { val xa: HikariTransactor[Task] = hxa }
      }
  }
}
