package ru.sgu.switchmap.repositories

import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{DBBuild, Build, BuildNotFound}

object BuildRepository {

  trait Service {
    def get(): Task[List[Build]]
    def get(shortName: String): Task[Build]
    def create(build: DBBuild): Task[Boolean]
    def update(shortName: String, build: DBBuild): Task[Boolean]
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
    sql"SELECT b.name AS name, b.short_name AS shortName, COUNT(f.number) AS floorsNumber, COUNT(sw.name) AS switchesNumber FROM builds AS b JOIN floors AS f ON f.build_short_name = b.short_name JOIN switches ON sw.build_short_name = b.short_name AND sw.floor_number = f.number"
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
    sql"SELECT b.name AS name, b.short_name AS shortName, COUNT(f.number) AS floorsNumber, COUNT(sw.name) AS switchesNumber FROM builds AS b JOIN floors AS f ON f.build_short_name = b.short_name JOIN switches ON sw.build_short_name = b.short_name AND sw.floor_number = f.number WHERE short_name = $shortName"
      .query[Build]
      .option
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        maybeBuild =>
          Task.require(BuildNotFound(shortName))(Task.succeed(maybeBuild))
      )
  }

  def create(build: DBBuild): Task[Boolean] = {
    sql"INSERT INTO builds (name, short_name) VALUES (${build.name}, ${build.shortName})".update.run
      .transact(xa)
      .fold(_ => false, _ => true)
  }

  def update(
    shortName: String,
    build: DBBuild
  ): Task[Boolean] = {
    sql"UPDATE builds SET name = ${build.name}, shortName = ${build.shortName} WHERE short_name = $shortName".update.run
      .transact(xa)
      .fold(_ => false, _ => true)
  }

  def delete(
    shortName: String
  ): Task[Boolean] = {
    sql"DELETE FROM builds WHERE short_name = $shortName".update.run
      .transact(xa)
      .fold(_ => false, _ => true)
  }

}
