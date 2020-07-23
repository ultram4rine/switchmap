package ru.sgu.switchmap.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import ru.sgu.switchmap.models.{Floor, FloorNotFoundError}

class FloorRepository(transactor: Transactor[IO]) {

  def getFloorsOf(build: String): Stream[IO, Floor] = {
    sql"SELECT number FROM floors WHERE build_short_name = $build"
      .query[Floor]
      .stream
      .transact(transactor)
  }

  def getNumberOfFloorsOf(build: String): IO[Int] = {
    sql"SELECT COUNT(number) FROM floors WHERE build_short_name = $build"
      .query[Int]
      .option
      .transact(transactor)
      .map { case Some(num) => num }
  }

  def getFloorByBuildAndNumber(
    build: String,
    number: Int
  ): IO[Either[FloorNotFoundError.type, Floor]] = {
    sql"SELECT number FROM floors WHERE build_short_name = $build AND number = $number"
      .query[Floor]
      .option
      .transact(transactor)
      .map {
        case Some(floor) => Right(floor)
        case None        => Left(FloorNotFoundError)
      }
  }

  def createFloor(floor: Floor): IO[Floor] = {
    sql"""
         INSERT INTO floors
         (number, build_name, build_short_name)
         VALUES (${floor.number}, ${floor.buildName}, ${floor.buildShortName})
         """.update.run
      .transact(transactor)
      .map { _ =>
        floor.copy(
          number = floor.number,
          buildName = floor.buildName,
          buildShortName = floor.buildShortName
        )
      }
  }

  def deleteFloor(
    build: String,
    number: Int
  ): IO[Either[FloorNotFoundError.type, Unit]] = {
    sql"DELETE FROM floors WHERE build_short_name = $build AND number = $number".update.run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(())
        } else {
          Left(FloorNotFoundError)
        }
      }
  }

}
