package ru.sgu.switchmap

import doobie.quill.DoobieContext
import io.getquill._
import ru.sgu.switchmap.models._
import zio.{Has, RIO}
import zio.logging.Logging

package object repositories {
  type BuildRepository = Has[BuildRepository.Service]
  type FloorRepository = Has[FloorRepository.Service]
  type SwitchRepository = Has[SwitchRepository.Service]

  private[repositories] object Tables {
    val ctx = new DoobieContext.Postgres(Literal)
    import ctx._

    val builds = quote(
      querySchema[BuildRequest](
        "builds",
        _.name -> "name",
        _.shortName -> "short_name"
      )
    )

    val floors = quote(
      querySchema[FloorRequest](
        "floors",
        _.number -> "number",
        _.buildName -> "build_name",
        _.buildShortName -> "build_short_name"
      )
    )

    val switches = quote(
      querySchema[SwitchResponse](
        "switches",
        _.name -> "name",
        _.ip -> "ip",
        _.mac -> "mac",
        _.revision -> "revision",
        _.serial -> "serial",
        _.buildShortName -> "build_short_name",
        _.floorNumber -> "floor_number",
        _.positionTop -> "position_top",
        _.positionLeft -> "position_left",
        _.upSwitchName -> "up_switch_name",
        _.upLink -> "up_link"
      )
    )
  }

  def getBuilds(): RIO[BuildRepository, List[BuildResponse]] =
    RIO.accessM(_.get.get())
  def getBuild(shortName: String): RIO[BuildRepository, BuildResponse] =
    RIO.accessM(_.get.get(shortName))
  def createBuild(b: BuildRequest): RIO[BuildRepository, Boolean] =
    RIO.accessM(_.get.create(b))
  def updateBuild(
    shortName: String,
    b: BuildRequest
  ): RIO[BuildRepository, Boolean] =
    RIO.accessM(_.get.update(shortName, b))
  def deleteBuild(shortName: String): RIO[BuildRepository, Boolean] =
    RIO.accessM(_.get.delete(shortName))

  def getFloorsOf(build: String): RIO[FloorRepository, List[FloorResponse]] =
    RIO.accessM(_.get.getOf(build))
  def getFloor(
    build: String,
    number: Int
  ): RIO[FloorRepository, FloorResponse] =
    RIO.accessM(_.get.get(build, number))
  def createFloor(f: FloorRequest): RIO[FloorRepository, Boolean] =
    RIO.accessM(_.get.create(f))
  def deleteFloor(build: String, number: Int): RIO[FloorRepository, Boolean] =
    RIO.accessM(_.get.delete(build, number))

  def sync(): RIO[SwitchRepository with Logging, Unit] =
    RIO.accessM(_.get.sync())
  def snmpCommunities(): RIO[SwitchRepository, List[String]] =
    RIO.accessM(_.get.snmp())
  def getSwitches(): RIO[SwitchRepository, List[SwitchResponse]] =
    RIO.accessM(_.get.get())
  def getSwitchesOf(
    build: String
  ): RIO[SwitchRepository, List[SwitchResponse]] =
    RIO.accessM(_.get.getOf(build))
  def getSwitchesOf(
    build: String,
    floor: Int
  ): RIO[SwitchRepository, List[SwitchResponse]] =
    RIO.accessM(_.get.getOf(build, floor))
  def getUnplacedSwitchesOf(
    build: String
  ): RIO[SwitchRepository, List[SwitchResponse]] =
    RIO.accessM(_.get.getUnplacedOf(build))
  def getPlacedSwitchesOf(
    build: String,
    floor: Int
  ): RIO[SwitchRepository, List[SwitchResponse]] =
    RIO.accessM(_.get.getPlacedOf(build, floor))
  def getSwitch(name: String): RIO[SwitchRepository, SwitchResponse] =
    RIO.accessM(_.get.get(name))
  def createSwitch(sw: SwitchRequest): RIO[SwitchRepository, SwitchResult] =
    RIO.accessM(_.get.create(sw))
  def updateSwitch(
    name: String,
    sw: SwitchRequest
  ): RIO[SwitchRepository, SwitchResult] =
    RIO.accessM(_.get.update(name, sw))
  def updateSwitchPosition(
    name: String,
    position: SavePositionRequest
  ): RIO[SwitchRepository, Boolean] = RIO.accessM(_.get.update(name, position))
  def deleteSwitch(name: String): RIO[SwitchRepository, Boolean] =
    RIO.accessM(_.get.delete(name))
}
