package ru.sgu.switchmap.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import fs2.Stream
import ru.sgu.switchmap.models.{Build, BuildNotFoundError}

class BuildRepository(transactor: Transactor[IO]) {

  def getBuilds: Stream[IO, Build] = {
    sql"SELECT name, short_name FROM builds"
      .query[Build]
      .stream
      .transact(transactor)
  }

  def getBuildByShortName(
    shortName: String
  ): IO[Either[BuildNotFoundError.type, Build]] = {
    sql"SELECT name, short_name FROM builds WHERE short_name = $shortName"
      .query[Build]
      .option
      .transact(transactor)
      .map {
        case Some(build) => Right(build)
        case None        => Left(BuildNotFoundError)
      }
  }

  def createBuild(build: Build): Update0 = {
    sql"INSERT INTO builds (name, short_name) VALUES (${build.name}, ${build.shortName})".update
  }

  def updateBuild(
    shortName: String,
    build: Build
  ): IO[Either[BuildNotFoundError.type, Build]] = {
    sql"UPDATE builds SET name = ${build.name}, shortName = ${build.shortName} WHERE short_name = $shortName".update.run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(build.copy(name = build.name, shortName = build.shortName))
        } else {
          Left(BuildNotFoundError)
        }
      }
  }

  def deleteBuild(
    shortName: String
  ): IO[Either[BuildNotFoundError.type, Unit]] = {
    sql"DELETE FROM builds WHERE short_name = $shortName".update.run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(())
        } else {
          Left(BuildNotFoundError)
        }
      }
  }

}
