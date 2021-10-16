package ru.sgu.switchmap

import doobie.Transactor
import zio.{Managed, Task, URLayer, ZLayer, Has}
import zio.interop.catz._

import ru.sgu.switchmap.config.DBConfig

package object db {
  type DBTransactor = Has[DBTransactor.Resource]

  object DBTransactor {
    trait Resource {
      val xa: Transactor[Task]
    }

    val live: URLayer[Has[DBConfig], DBTransactor] = ZLayer.fromService { db =>
      new Resource {
        val xa: Transactor[Task] =
          Transactor.fromDriverManager(
            "org.postgresql.Driver",
            db.url,
            db.user,
            db.password
          )
      }
    }
  }
}
