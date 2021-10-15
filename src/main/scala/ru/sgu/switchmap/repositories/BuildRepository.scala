package ru.sgu.switchmap.repositories

import cats.effect.Blocker
import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import ru.sgu.switchmap.models.{Build, BuildNotFound}

object BuildRepository {

  trait Service {
    def get(): Task[List[Build]]
    def get(shortName: String): Task[Build]
    def create(build: Build): Task[Build]
    def update(shortName: String, build: Build): Task[Build]
    def delete(shortName: String): Task[Boolean]
  }

  val live: URLayer[DBTransactor, BuildRepository] =
    ZLayer.fromService { resource =>
      DoobieBuildRepository(resource.xa)
    }
}

private[repositories] final case class DoobieBuildRepository(
  xa: Transactor[Task]
) extends BuildRepository.Service {

  def get(): Task[List[Build]] = {
    sql"SELECT name, short_name FROM builds"
      .query[Build]
      .to[List]
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        builds => Task.succeed(builds)
      )
  }

  def get(
    shortName: String
  ): Task[Build] = {
    sql"SELECT name, short_name FROM builds WHERE short_name = $shortName"
      .query[Build]
      .option
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        maybeBuild =>
          Task.require(BuildNotFound(shortName))(Task.succeed(maybeBuild))
      )
  }

  def create(build: Build): Task[Build] = {
    sql"INSERT INTO builds (name, short_name) VALUES (${build.name}, ${build.shortName})".update.run
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(build))
  }

  def update(
    shortName: String,
    build: Build
  ): Task[Build] = {
    sql"UPDATE builds SET name = ${build.name}, shortName = ${build.shortName} WHERE short_name = $shortName".update.run
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(build))
  }

  def delete(
    shortName: String
  ): Task[Boolean] = {
    sql"DELETE FROM builds WHERE short_name = $shortName".update.run
      .transact(xa)
      .fold(_ => false, _ => true)
  }

}
