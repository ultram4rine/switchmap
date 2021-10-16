package ru.sgu.switchmap

import zio.{Has, RIO}
import ru.sgu.switchmap.models.{DBBuild, Build, DBFloor, Floor, Switch}

package object repositories {
  type BuildRepository = Has[BuildRepository.Service]
  type FloorRepository = Has[FloorRepository.Service]
  type SwitchRepository = Has[SwitchRepository.Service]

  def getBuilds(): RIO[BuildRepository, List[Build]] = RIO.accessM(_.get.get())
  def getBuild(shortName: String): RIO[BuildRepository, Build] =
    RIO.accessM(_.get.get(shortName))
  def createBuild(b: DBBuild): RIO[BuildRepository, Boolean] =
    RIO.accessM(_.get.create(b))
  def updateBuild(
    shortName: String,
    b: DBBuild
  ): RIO[BuildRepository, Boolean] =
    RIO.accessM(_.get.update(shortName, b))
  def deleteBuild(shortName: String): RIO[BuildRepository, Boolean] =
    RIO.accessM(_.get.delete(shortName))

  def getFloorsOf(build: String): RIO[FloorRepository, List[Floor]] =
    RIO.accessM(_.get.getOf(build))
  def getFloor(build: String, number: Int): RIO[FloorRepository, Floor] =
    RIO.accessM(_.get.get(build, number))
  def createFloor(f: DBFloor): RIO[FloorRepository, Boolean] =
    RIO.accessM(_.get.create(f))
  def deleteFloor(build: String, number: Int): RIO[FloorRepository, Boolean] =
    RIO.accessM(_.get.delete(build, number))

  def getSwitches(): RIO[SwitchRepository, List[Switch]] =
    RIO.accessM(_.get.get())
  def getSwitchesOf(build: String): RIO[SwitchRepository, List[Switch]] =
    RIO.accessM(_.get.getOf(build))
  def getSwitchesOf(
    build: String,
    floor: Int
  ): RIO[SwitchRepository, List[Switch]] =
    RIO.accessM(_.get.getOf(build, floor))
  def getSwitch(name: String): RIO[SwitchRepository, Switch] =
    RIO.accessM(_.get.get(name))
  def createSwitch(sw: Switch): RIO[SwitchRepository, Switch] =
    RIO.accessM(_.get.create(sw))
  def updateSwitch(name: String, sw: Switch): RIO[SwitchRepository, Switch] =
    RIO.accessM(_.get.update(name, sw))
  def deleteSwitch(name: String): RIO[SwitchRepository, Boolean] =
    RIO.accessM(_.get.delete(name))
}
