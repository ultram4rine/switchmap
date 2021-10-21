package ru.sgu.switchmap.repositories

import doobie.implicits._
import doobie.{Query0, Update0}
import doobie.hikari.HikariTransactor
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{DBFloor, Floor, FloorNotFound, Switch}

object FloorRepository {

  trait Service {
    def getOf(build: String): Task[List[Floor]]
    def get(build: String, number: Int): Task[Floor]
    def create(floor: DBFloor): Task[Boolean]
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

  def getOf(build: String): Task[List[Floor]] = {
    val q = quote {
      Tables.floors
        .leftJoin(Tables.switches)
        .on((f, sw) => f.number == sw.floorNumber.getOrNull)
        .filter {
          case (f, _) =>
            f.buildShortName == lift(build)
        }
        .sortBy(_._1.number)
        .groupBy { case (f, _) => f.number }
        .map {
          case (number, rows) =>
            Floor(
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
  ): Task[Floor] = {
    val q = quote {
      Tables.floors
        .leftJoin(Tables.switches)
        .on((f, sw) => f.number == sw.floorNumber.getOrNull)
        .filter {
          case (f, _) =>
            f.buildShortName == lift(build) && f.number == lift(number)
        }
        .sortBy(_._1.number)
        .groupBy { case (f, _) => f.number }
        .map {
          case (number, rows) =>
            Floor(
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

  def create(floor: DBFloor): Task[Boolean] = {
    val q = quote {
      Tables.floors.insert(lift(floor))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
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
      .fold(_ => false, _ => true)
  }

}
