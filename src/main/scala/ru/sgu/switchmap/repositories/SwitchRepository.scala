package ru.sgu.switchmap.repositories

import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.models.{Switch, SwitchNotFound}

object SwitchRepository {

  trait Service {
    def get(): Task[List[Switch]]
    def getOf(build: String): Task[List[Switch]]
    def getOf(build: String, floor: Int): Task[List[Switch]]
    def get(name: String): Task[Switch]
    def create(switch: Switch): Task[Boolean]
    def update(name: String, switch: Switch): Task[Boolean]
    def delete(name: String): Task[Boolean]
  }

  val live: URLayer[DBTransactor, SwitchRepository] =
    ZLayer.fromService { resource =>
      DoobieSwitchRepository(resource.xa)
    }
}

private[repositories] final case class DoobieSwitchRepository(
  xa: Transactor[Task]
) extends SwitchRepository.Service {

  import Tables.ctx._

  def get(): Task[List[Switch]] = {
    val q = quote {
      Tables.switches.sortBy(sw => sw.name)
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def getOf(build: String): Task[List[Switch]] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.buildShortName.getOrNull == lift(build))
        .sortBy(sw => sw.name)
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def getOf(build: String, floor: Int): Task[List[Switch]] = {
    val q = quote {
      Tables.switches
        .filter(sw =>
          sw.buildShortName.getOrNull == lift(
            build
          ) && sw.floorNumber.getOrNull == lift(floor)
        )
        .sortBy(sw => sw.name)
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def get(
    name: String
  ): Task[Switch] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .map(_.headOption)
      .foldM(
        err => Task.fail(err),
        maybeSwitch =>
          Task.require(SwitchNotFound(name))(Task.succeed(maybeSwitch))
      )
  }

  def create(switch: Switch): Task[Boolean] = {
    val q = quote {
      Tables.switches.insert(lift(switch))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
  }

  def update(
    name: String,
    switch: Switch
  ): Task[Boolean] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
        .update(lift(switch))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
  }

  def delete(name: String): Task[Boolean] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
        .delete
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .fold(_ => false, _ => true)
  }

}
