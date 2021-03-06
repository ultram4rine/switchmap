package repositories

import javax.inject.{Inject, Singleton}
import models.{Build, Floor, Switch}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(
  implicit ec: ExecutionContext
) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class BuildsTable(tag: Tag) extends Table[Build](tag, "builds") {

    def name = column[String]("name", O.Unique)
    def shortName = column[String]("short_name", O.Unique)

    def * = (name, shortName).mapTo[Build]

    def pk = primaryKey("build_pk", (name, shortName))
  }

  private class FloorsTable(tag: Tag) extends Table[Floor](tag, "floors") {

    def number = column[Int]("number")
    def buildName = column[String]("build_name")
    def buildShortName = column[String]("build_short_name")

    def * =
      (number, buildName, buildShortName).mapTo[Floor]

    def pk = primaryKey("floor_pk", (number, buildShortName))

    def build =
      foreignKey(
        "build_fk",
        (buildName, buildShortName),
        builds
      )(
        b => (b.name, b.shortName),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.Cascade
      )
  }

  private class SwitchesTable(tag: Tag)
      extends Table[Switch](
        tag,
        "switches"
      ) {

    def name = column[String]("name", O.Unique)
    def ip = column[String]("ip", O.Unique)
    def mac = column[String]("mac", O.Unique)
    def snmpCommunity = column[String]("snmp_community")
    def revision = column[Option[String]]("revision")
    def serial = column[Option[String]]("serial", O.Unique)
    def portsNumber = column[Option[Int]]("ports_number")
    def buildShortName = column[Option[String]]("build_short_name")
    def floorNumber = column[Option[Int]]("floor_number")
    def positionTop = column[Option[Float]]("position_top")
    def positionLeft = column[Option[Float]]("position_left")
    def upSwitchName = column[Option[String]]("up_switch_name")
    def upSwitchMAC = column[Option[String]]("up_switch_mac")
    def upLink = column[Option[String]]("up_link")

    def * =
      (
        name,
        ip,
        mac,
        snmpCommunity,
        revision,
        serial,
        portsNumber,
        buildShortName,
        floorNumber,
        positionTop,
        positionLeft,
        upSwitchName,
        upSwitchMAC,
        upLink
      ).mapTo[Switch]

    def pk = primaryKey("switch_pk", (name, mac))

    def floor =
      foreignKey("fk_floor", (buildShortName.get, floorNumber.get), floors)(
        f => (f.buildShortName, f.number),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.NoAction
      )

    def switch =
      foreignKey(
        name = "fk_switch",
        (upSwitchName.get, upSwitchMAC.get),
        switches
      )(
        sw => (sw.name, sw.mac),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.NoAction
      )
  }

  private val builds = TableQuery[BuildsTable]
  private val floors = TableQuery[FloorsTable]
  private val switches = TableQuery[SwitchesTable]

  def getBuilds: Future[Seq[Build]] = db.run { builds.result }

  def getBuildByShortName(buildShortName: String): Future[Option[Build]] =
    db.run { builds.filter(_.shortName === buildShortName).result.headOption }

  def getFloorOf(buildAddr: String): Future[Seq[Floor]] =
    db.run {
      floors.filter(_.buildShortName === buildAddr).sortBy(_.number).result
    }

  def getFloorByShortNameAndNum(
    buildShortName: String,
    floorNumber: Int
  ): Future[Option[Floor]] =
    db.run {
      floors
        .filter(floor =>
          floor.buildShortName === buildShortName && floor.number === floorNumber
        )
        .result
        .headOption
    }

  def getSwitches: Future[Seq[Switch]] = db.run { switches.result }

  def getSwitchByName(switchName: String): Future[Option[Switch]] =
    db.run { switches.filter(_.name === switchName).result.headOption }

  def getSwitchesOfBuild(buildShortName: String): Future[Seq[Switch]] =
    db.run { switches.filter(_.buildShortName === buildShortName).result }

  def getSwitchesOfFloor(
    buildShortName: String,
    floorNumber: Int
  ): Future[Seq[Switch]] =
    db.run {
      switches
        .filter(_.buildShortName === buildShortName)
        .filter(_.floorNumber === floorNumber)
        .result
    }

  def createBuild(b: Build): Future[Int] = { db.run(builds += b) }

  def updateBuild(buildShortName: String, b: Build): Future[Int] = {
    db.run(builds.filter(_.shortName === buildShortName).update(b))
  }

  def deleteBuild(b: Build): Future[Int] = {
    db.run(
      builds
        .filter(build =>
          build.name === b.name && build.shortName === b.shortName
        )
        .delete
    )
  }

  def createFloor(f: Floor): Future[Int] = { db.run(floors += f) }

  def deleteFloor(f: Floor): Future[Int] = {
    db.run(
      floors
        .filter(floor =>
          floor.buildShortName === f.buildShortName && floor.number === f.number
        )
        .delete
    )
  }

  def createSwitch(sw: Switch): Future[Int] = { db.run(switches += sw) }
}
