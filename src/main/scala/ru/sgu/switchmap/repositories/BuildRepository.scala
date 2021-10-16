package ru.sgu.switchmap.repositories

import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{DBBuild, Build, BuildNotFound, DBFloor, Switch}

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

  import Tables.ctx._

  def get(): Task[List[Build]] = {
    val q = quote {
      Tables.builds
        .leftJoin(Tables.floors)
        .on((b, f) => b.shortName == f.buildShortName)
        .leftJoin(Tables.switches)
        .on((bf, sw) =>
          bf._1.shortName == sw.buildShortName.getOrNull && bf._2.getOrNull.number == sw.floorNumber.getOrNull
        )
        .sortBy(_._1._1.shortName)
        .groupBy { case (b, _) => (b._1.name, b._1.shortName) }
        .map {
          case ((name, shortName), rows) =>
            Build(
              name,
              shortName,
              rows.map(_._1._2.map(_.number)).size,
              rows.map(_._2.map(_.name)).size
            )
        }
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        builds => Task.succeed(builds)
      )
  }

  def get(shortName: String): Task[Build] = {
    val q = quote {
      Tables.builds
        .leftJoin(Tables.floors)
        .on((b, f) => b.shortName == f.buildShortName)
        .leftJoin(Tables.switches)
        .on((bf, sw) =>
          bf._1.shortName == sw.buildShortName.getOrNull && bf._2.getOrNull.number == sw.floorNumber.getOrNull
        )
        .filter { case (bf, _) => bf._1.shortName == lift(shortName) }
        .sortBy(_._1._1.shortName)
        .groupBy { case (b, _) => (b._1.name, b._1.shortName) }
        .map {
          case ((name, shortName), rows) =>
            Build(
              name,
              shortName,
              rows.map(_._1._2.map(_.number)).size,
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
        maybeBuild =>
          Task.require(BuildNotFound(shortName))(Task.succeed(maybeBuild))
      )
  }

  def create(build: DBBuild): Task[Boolean] = {
    val q = quote {
      Tables.builds.insert(lift(build))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
  }

  def update(
    shortName: String,
    build: DBBuild
  ): Task[Boolean] = {
    val q = quote {
      Tables.builds
        .filter(_.shortName == lift(shortName))
        .update(lift(build))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
  }

  def delete(
    shortName: String
  ): Task[Boolean] = {
    val q = quote {
      Tables.builds
        .filter(_.shortName == lift(shortName))
        .delete
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
  }

}
