package ru.sgu.switchmap.repositories

import cats.effect.Blocker
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
    def getNumberOf(build: String): Task[Int]
    def getOf(build: String, floor: Int): Task[List[Switch]]
    def getNumberOf(build: String, floor: Int): Task[Int]
    def get(name: String): Task[Switch]
    def create(switch: Switch): Task[Switch]
    def update(name: String, switch: Switch): Task[Switch]
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

  def get(): Task[List[Switch]] = {
    sql"""
          SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
          floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
         """
      .query[Switch]
      .to[List]
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def getOf(build: String): Task[List[Switch]] = {
    sql"""
          SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
          floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
          WHERE build_short_name = $build
          """
      .query[Switch]
      .to[List]
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def getNumberOf(build: String): Task[Int] = {
    sql"SELECT COUNT(name) FROM switches WHERE build_short_name = $build"
      .query[Int]
      .unique
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        num => Task.succeed(num)
      )
  }

  def getOf(build: String, floor: Int): Task[List[Switch]] = {
    sql"""
         SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
         floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
         WHERE build_short_name = $build AND floor_number = $floor
         """
      .query[Switch]
      .to[List]
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        num => Task.succeed(num)
      )
  }

  def getNumberOf(build: String, floor: Int): Task[Int] = {
    sql"SELECT COUNT(name) FROM switches WHERE build_short_name = $build AND floor_number = $floor"
      .query[Int]
      .unique
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        num => Task.succeed(num)
      )
  }

  def get(
    name: String
  ): Task[Switch] = {
    sql"""
         SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
         floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
         WHERE name = $name
         """
      .query[Switch]
      .option
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        maybeSwitch =>
          Task.require(SwitchNotFound(name))(Task.succeed(maybeSwitch))
      )
  }

  def create(switch: Switch): Task[Switch] = {
    sql"""
         INSERT INTO switches
         (name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
         floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link)
         VALUES (${switch.name}, ${switch.ip}, ${switch.mac}, ${switch.snmpCommunity}, ${switch.revision},
         ${switch.serial}, ${switch.portsNumber}, ${switch.buildShortName}, ${switch.floorNumber},
         ${switch.positionTop}, ${switch.positionLeft}, ${switch.upSwitchName}, ${switch.upSwitchMAC}, ${switch.upLink})
         """.update.run
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        _ => Task.succeed(switch)
      )
  }

  def update(
    name: String,
    switch: Switch
  ): Task[Switch] = {
    sql"""
         UPDATE switches
         SET name = ${switch.name}, ip = ${switch.ip}, mac = ${switch.mac}, snmp_community = ${switch.snmpCommunity},
         revision = ${switch.revision}, serial = ${switch.serial}, ports_number = ${switch.portsNumber},
         build_short_name = ${switch.buildShortName}, floor_number = ${switch.floorNumber},
         position_top = ${switch.positionTop}, position_left = ${switch.positionLeft},
         up_switch_name = ${switch.upSwitchName}, up_switch_mac = ${switch.upSwitchMAC}, up_link = ${switch.upLink}
         WHERE name = $name
         """.update.run
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(switch))
    /* .map { affectedRows =>
        if (affectedRows == 1) {
          Right(
            switch.copy(
              name = switch.name,
              ip = switch.ip,
              mac = switch.mac,
              snmpCommunity = switch.snmpCommunity,
              revision = switch.revision,
              serial = switch.serial,
              portsNumber = switch.portsNumber,
              buildShortName = switch.buildShortName,
              floorNumber = switch.floorNumber,
              positionTop = switch.positionTop,
              positionLeft = switch.positionLeft,
              upSwitchName = switch.upSwitchName,
              upSwitchMAC = switch.upSwitchMAC,
              upLink = switch.upLink
            )
          )
        } else {
          Left(SwitchNotFoundError)
        }
      } */
  }

  def delete(name: String): Task[Boolean] = {
    sql"DELETE FROM switches WHERE name = $name".update.run
      .transact(xa)
      .fold(_ => false, _ => true)
    /* .map { affectedRows =>
        if (affectedRows == 1) {
          Right(())
        } else {
          Left(SwitchNotFoundError)
        }
      } */
  }

}
