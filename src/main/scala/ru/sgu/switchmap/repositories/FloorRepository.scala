package ru.sgu.switchmap.repositories

import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{DBFloor, Floor, FloorNotFound}

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
  tnx: Transactor[Task]
) extends FloorRepository.Service {

  def getOf(build: String): Task[List[Floor]] = {
    sql"SELECT f.number AS number, COUNT(sw.name) AS switches_number FROM floors AS f LEFT JOIN switches AS sw ON sw.floor_number = f.number WHERE f.build_short_name = $build GROUP BY f.number ORDER BY f.number ASC"
      .query[Floor]
      .to[List]
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        floors => Task.succeed(floors)
      )
  }

  def get(
    build: String,
    number: Int
  ): Task[Floor] = {
    sql"SELECT f.number AS number, COUNT(sw.name) AS switchesNumber FROM floors AS f LEFT JOIN switches AS sw ON sw.floor_number = f.number WHERE f.build_short_name = $build AND f.number = $number GROUP BY f.number"
      .query[Floor]
      .option
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        maybeFloor =>
          Task.require(FloorNotFound(number, build))(Task.succeed(maybeFloor))
      )
  }

  def create(floor: DBFloor): Task[Boolean] = {
    sql"""
         INSERT INTO floors
         (number, build_name, build_short_name)
         VALUES (${floor.number}, ${floor.buildName}, ${floor.buildShortName})
         """.update.run
      .transact(tnx)
      .fold(_ => false, _ => true)
  }

  def delete(
    build: String,
    number: Int
  ): Task[Boolean] = {
    sql"DELETE FROM floors WHERE build_short_name = $build AND number = $number".update.run
      .transact(tnx)
      .fold(_ => false, _ => true)
  }

}
