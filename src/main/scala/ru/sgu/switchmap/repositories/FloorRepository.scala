package ru.sgu.switchmap.repositories

import doobie.implicits._
import doobie.hikari.HikariTransactor
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{FloorRequest, FloorResponse, FloorNotFound}

object FloorRepository {

  trait Service {
    def getOf(build: String): Task[List[FloorResponse]]
    def get(build: String, number: Int): Task[FloorResponse]
    def create(floor: FloorRequest): Task[Boolean]
    def delete(build: String, number: Int): Task[Boolean]
  }

  val live: URLayer[DBTransactor, FloorRepository] =
    ZLayer.fromService { resource =>
      DoobieFloorRepository(resource.xa)
    }
}

private[repositories] final case class DoobieFloorRepository(
  xa: HikariTransactor[Task]
) extends FloorRepository.Service {

  import Tables.ctx._

  def getOf(build: String): Task[List[FloorResponse]] = {
    val q = quote {
      Tables.floors
        .leftJoin(Tables.switches)
        .on((f, sw) =>
          sw.buildShortName.getOrNull == lift(
            build
          ) && sw.floorNumber.getOrNull == f.number
        )
        .filter { case (f, _) =>
          f.buildShortName == lift(build)
        }
        .sortBy(_._1.number)
        .groupBy { case (f, _) => f.number }
        .map { case (number, rows) =>
          FloorResponse(
            number,
            rows.map(_._2.map(_.name)).size
          )
        }
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        floors => Task.succeed(floors)
      )
  }

  def get(
    build: String,
    number: Int
  ): Task[FloorResponse] = {
    val q = quote {
      Tables.floors
        .leftJoin(Tables.switches)
        .on((f, sw) =>
          sw.buildShortName.getOrNull == lift(
            build
          ) && sw.floorNumber.getOrNull == f.number
        )
        .filter { case (f, _) =>
          f.buildShortName == lift(build) && f.number == lift(number)
        }
        .sortBy(_._1.number)
        .groupBy { case (f, _) => f.number }
        .map { case (number, rows) =>
          FloorResponse(
            number,
            rows.map(_._2.map(_.name)).size
          )
        }
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .map(_.headOption)
      .foldM(
        err => Task.fail(err),
        maybeFloor =>
          Task.require(FloorNotFound(number, build))(Task.succeed(maybeFloor))
      )
  }

  def create(floor: FloorRequest): Task[Boolean] = {
    val q = quote {
      Tables.floors.insert(lift(floor))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(true))
  }

  def delete(
    build: String,
    number: Int
  ): Task[Boolean] = {
    val q = quote {
      Tables.floors
        .filter(f =>
          f.buildShortName == lift(build) && f.number == lift(number)
        )
        .delete
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(true))
  }

}
