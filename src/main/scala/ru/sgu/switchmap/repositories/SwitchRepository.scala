package ru.sgu.switchmap.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import ru.sgu.switchmap.models.{Switch, SwitchNotFoundError}

class SwitchRepository(transactor: Transactor[IO]) {

  def getSwitches: Stream[IO, Switch] = {
    sql"""
          SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
          floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
         """
      .query[Switch]
      .stream
      .transact(transactor)
  }

  def getSwitchesOfBuild(build: String): Stream[IO, Switch] = {
    sql"""
          SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
          floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
          WHERE build_short_name = $build
          """
      .query[Switch]
      .stream
      .transact(transactor)
  }

  def getNumberOfSwitchesOfBuild(build: String): IO[Int] = {
    sql"SELECT COUNT(name) FROM switches WHERE build_short_name = $build"
      .query[Int]
      .option
      .transact(transactor)
      .map { case Some(num) => num }
  }

  def getSwitchesOfFloor(build: String, floor: Int): Stream[IO, Switch] = {
    sql"""
         SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
         floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
         WHERE build_short_name = $build AND floor_number = $floor
         """
      .query[Switch]
      .stream
      .transact(transactor)
  }

  def getNumberOfSwitchesOfFloor(build: String, floor: Int): IO[Int] = {
    sql"SELECT COUNT(name) FROM switches WHERE build_short_name = $build AND floor_number = $floor"
      .query[Int]
      .option
      .transact(transactor)
      .map { case Some(num) => num }
  }

  def getSwitchByName(
    name: String
  ): IO[Either[SwitchNotFoundError.type, Switch]] = {
    sql"""
         SELECT name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
         floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link FROM switches
         WHERE name = $name
         """
      .query[Switch]
      .option
      .transact(transactor)
      .map {
        case Some(switch) => Right(switch)
        case None         => Left(SwitchNotFoundError)
      }
  }

  def createSwitch(switch: Switch): IO[Switch] = {
    sql"""
         INSERT INTO switches
         (name, ip, mac, snmp_community, revision, serial, ports_number, build_short_name,
         floor_number, position_top, position_left, up_switch_name, up_switch_mac, up_link)
         VALUES (${switch.name}, ${switch.ip}, ${switch.mac}, ${switch.snmpCommunity}, ${switch.revision},
         ${switch.serial}, ${switch.portsNumber}, ${switch.buildShortName}, ${switch.floorNumber},
         ${switch.positionTop}, ${switch.positionLeft}, ${switch.upSwitchName}, ${switch.upSwitchMAC}, ${switch.upLink})
         """.update.run
      .transact(transactor)
      .map { _ =>
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
      }
  }

  def updateSwitch(
    name: String,
    switch: Switch
  ): IO[Either[SwitchNotFoundError.type, Switch]] = {
    sql"""
         UPDATE switches
         SET name = ${switch.name}, ip = ${switch.ip}, mac = ${switch.mac}, snmp_community = ${switch.snmpCommunity},
         revision = ${switch.revision}, serial = ${switch.serial}, ports_number = ${switch.portsNumber},
         build_short_name = ${switch.buildShortName}, floor_number = ${switch.floorNumber},
         position_top = ${switch.positionTop}, position_left = ${switch.positionLeft},
         up_switch_name = ${switch.upSwitchName}, up_switch_mac = ${switch.upSwitchMAC}, up_link = ${switch.upLink}
         WHERE name = $name
         """.update.run
      .transact(transactor)
      .map { affectedRows =>
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
      }
  }

  def deleteSwitch(name: String): IO[Either[SwitchNotFoundError.type, Unit]] = {
    sql"DELETE FROM switches WHERE name = $name".update.run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(())
        } else {
          Left(SwitchNotFoundError)
        }
      }
  }

}
