package repositories

import javax.inject.{Inject, Singleton}
import models.{Build, Floor, Switch}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

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
    def shortName = column[String]("addr", O.Unique)

    def * = (name, shortName).mapTo[Build]

    def pk = primaryKey("build_pk", (name, shortName))
  }

  private class FloorsTable(tag: Tag) extends Table[Floor](tag, "floors") {
    def number = column[Int]("number")

    def buildName = column[String]("build_name")

    def buildShortName = column[String]("build_addr")

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

    def ip = column[String]("ip")

    def mac = column[String]("mac", O.Unique)

    def vendor = column[String]("vendor")

    def revision = column[Option[String]]("revision")

    def serial = column[Option[String]]("serial")

    def upSwitch = column[Option[Int]]("switch_id")

    def port = column[Option[String]]("port")

    def posTop = column[Int]("pos_top")

    def posLeft = column[Int]("pos_left")

    def buildShortName = column[String]("build_addr")

    def floorNumber = column[Int]("floor_number")

    def * =
      (
        name,
        ip,
        mac,
        vendor,
        revision,
        serial,
        upSwitch,
        port,
        posTop,
        posLeft,
        buildShortName,
        floorNumber
      ).mapTo[Switch]

    def pk = primaryKey("switch_pk", (name, mac))

    def floor =
      foreignKey("floor_fk", (floorNumber, buildShortName), floors)(
        f => (f.number, f.buildShortName),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.Cascade
      )
  }

  private val builds = TableQuery[BuildsTable]
  private val floors = TableQuery[FloorsTable]
  private val switches = TableQuery[SwitchesTable]

  private val tables = List(builds, floors, switches)
  private val existing: Future[Vector[MTable]] = db.run { MTable.getTables }

  def createIfNotExist: Future[List[Unit]] =
    existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist = tables
        .filter(table => !names.contains(table.baseTableRow.tableName))
        .map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExist))
    })

  def getBuilds: Future[Seq[Build]] = db.run { builds.result }

  def getBuildByAddr(buildAddr: String): Future[Option[Build]] =
    db.run { builds.filter(_.shortName === buildAddr).result.headOption }

  def getFloorOf(buildAddr: String): Future[Seq[Floor]] =
    db.run {
      floors.filter(_.buildShortName === buildAddr).sortBy(_.number).result
    }

  def getFloorByAddrAndNum(
    buildAddr: String,
    floorNumber: Int
  ): Future[Option[Floor]] =
    db.run {
      floors
        .filter(floor =>
          floor.buildShortName === buildAddr && floor.number === floorNumber
        )
        .result
        .headOption
    }

  def getSwitches: Future[Seq[Switch]] = db.run { switches.result }

  def getSwitchByName(switchName: String): Future[Option[Switch]] =
    db.run { switches.filter(_.name === switchName).result.headOption }

  def getSwitchesOfBuild(buildAddr: String): Future[Seq[Switch]] =
    db.run { switches.filter(_.buildShortName === buildAddr).result }

  def getSwitchesOfFloor(
    buildAddr: String,
    floorNumber: Int
  ): Future[Seq[Switch]] =
    db.run {
      switches
        .filter(_.buildShortName === buildAddr)
        .filter(_.floorNumber === floorNumber)
        .result
    }

  def createBuild(b: Build): Future[Int] = { db.run(builds += b) }

  def updateBuild(buildAddr: String, b: Build): Future[Int] = {
    db.run(builds.filter(_.shortName === buildAddr).update(b))
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
          floor.buildShortName === f.buildAddr && floor.number === f.number
        )
        .delete
    )
  }

  def createSwitch(sw: Switch): Future[Int] = { db.run(switches += sw) }
}
