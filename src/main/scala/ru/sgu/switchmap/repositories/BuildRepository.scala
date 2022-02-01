package ru.sgu.switchmap.repositories

import doobie.hikari.HikariTransactor
import doobie.implicits._
import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{BuildNotFound, BuildRequest, BuildResponse}
import zio._
import zio.interop.catz._

object BuildRepository {

  trait Service {
    def get(): Task[List[BuildResponse]]
    def get(shortName: String): Task[BuildResponse]
    def create(build: BuildRequest): Task[Boolean]
    def update(shortName: String, build: BuildRequest): Task[Boolean]
    def delete(shortName: String): Task[Boolean]
  }

  val live: URLayer[DBTransactor, BuildRepository] =
    ZLayer.fromService { resource =>
      DoobieBuildRepository(resource.xa)
    }
}

private[repositories] final case class DoobieBuildRepository(
  xa: HikariTransactor[Task]
) extends BuildRepository.Service {

  import Tables.ctx._

  def get(): Task[List[BuildResponse]] = {
    val q = quote {
      Tables.builds
        .leftJoin(Tables.floors)
        .on((b, f) => b.shortName == f.buildShortName)
        .leftJoin(Tables.switches)
        .on((bf, sw) => bf._1.shortName == sw.buildShortName.getOrNull)
        .sortBy(_._1._1.shortName)
        .groupBy { case (b, _) => (b._1.name, b._1.shortName) }
        .map { case ((name, shortName), rows) =>
          BuildResponse(
            name,
            shortName,
            rows.map(_._1._2.map(_.number)).distinct.size,
            rows.map(_._2.map(_.name)).distinct.size
          )
        }
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldZIO(
        err => Task.fail(err),
        builds => Task.succeed(builds)
      )
  }

  def get(shortName: String): Task[BuildResponse] = {
    val q = quote {
      Tables.builds
        .leftJoin(Tables.floors)
        .on((b, f) => b.shortName == f.buildShortName)
        .leftJoin(Tables.switches)
        .on((bf, sw) => bf._1.shortName == sw.buildShortName.getOrNull)
        .filter { case (bf, _) => bf._1.shortName == lift(shortName) }
        .sortBy(_._1._1.shortName)
        .groupBy { case (b, _) => (b._1.name, b._1.shortName) }
        .map { case ((name, shortName), rows) =>
          BuildResponse(
            name,
            shortName,
            rows.map(_._1._2.map(_.number)).distinct.size,
            rows.map(_._2.map(_.name)).distinct.size
          )
        }
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .map(_.headOption)
      .foldZIO(
        err => Task.fail(err),
        maybeBuild =>
          Task.require(BuildNotFound(shortName))(Task.succeed(maybeBuild))
      )
  }

  def create(build: BuildRequest): Task[Boolean] = {
    val q = quote {
      Tables.builds.insert(lift(build))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldZIO(err => Task.fail(err), _ => Task.succeed(true))
  }

  def update(
    shortName: String,
    build: BuildRequest
  ): Task[Boolean] = {
    val q = quote {
      Tables.builds
        .filter(_.shortName == lift(shortName))
        .update(lift(build))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldZIO(err => Task.fail(err), _ => Task.succeed(true))
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
      .foldZIO(err => Task.fail(err), _ => Task.succeed(true))
  }

}
