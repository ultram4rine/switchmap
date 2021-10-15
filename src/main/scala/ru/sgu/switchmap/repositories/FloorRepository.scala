package ru.sgu.switchmap.repositories

import cats.effect.Blocker
import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import ru.sgu.switchmap.models.{Floor, FloorNotFound}

object FloorRepository {

  trait Service {
    def getOf(build: String): Task[List[Floor]]
    def getNumberOf(build: String): Task[Int]
    def get(build: String, number: Int): Task[Floor]
    def create(floor: Floor): Task[Floor]
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
    sql"SELECT number FROM floors WHERE build_short_name = $build"
      .query[Floor]
      .to[List]
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        floors => Task.succeed(floors)
      )
  }

  def getNumberOf(build: String): Task[Int] = {
    sql"SELECT COUNT(number) FROM floors WHERE build_short_name = $build"
      .query[Int]
      .unique
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        num => Task.succeed(num)
      )
  }

  def get(
    build: String,
    number: Int
  ): Task[Floor] = {
    sql"SELECT number FROM floors WHERE build_short_name = $build AND number = $number"
      .query[Floor]
      .option
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        maybeFloor =>
          Task.require(FloorNotFound(number, build))(Task.succeed(maybeFloor))
      )
  }

  def create(floor: Floor): Task[Floor] = {
    sql"""
         INSERT INTO floors
         (number, build_name, build_short_name)
         VALUES (${floor.number}, ${floor.buildName}, ${floor.buildShortName})
         """.update.run
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        _ => Task.succeed(floor)
      )
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
