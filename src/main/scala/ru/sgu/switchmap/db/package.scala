package ru.sgu.switchmap

import doobie.Transactor
import doobie.hikari.HikariTransactor
import zio.{ZManaged, Managed, Task, URLayer, ZLayer, Has}
import zio.interop.catz._
import zio.interop.catz.implicits._

import ru.sgu.switchmap.config.DBConfig
import doobie.util.ExecutionContexts
import zio.blocking.Blocking
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import zio.ZIO

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
