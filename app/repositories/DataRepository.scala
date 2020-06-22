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
    def addr = column[String]("addr", O.Unique)

    def * = (name, addr) <> ((Build.apply _).tupled, Build.unapply)

    def pk = primaryKey("build_pk", (name, addr))
  }

  private class FloorsTable(tag: Tag) extends Table[Floor](tag, "floors") {
    def number = column[Int]("number")

    def buildName = column[String]("build_name")

    def buildAddr = column[String]("build_addr")

    def * =
      (number, buildName, buildAddr) <> ((Floor.apply _).tupled, Floor.unapply)

    def pk = primaryKey("floor_pk", (number, buildAddr))

    def build =
      foreignKey(
        "build_fk",
        (buildName, buildAddr),
        builds
      )(
        b => (b.name, b.addr),
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

    def buildAddr = column[String]("build_addr")

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
        buildAddr,
        floorNumber
      ) <> ((Switch.apply _).tupled, Switch.unapply)

    def pk = primaryKey("switch_pk", (name, mac))

    def floor =
      foreignKey("floor_fk", (floorNumber, buildAddr), floors)(
        f => (f.number, f.buildAddr),
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
    db.run { builds.filter(_.addr === buildAddr).result.headOption }

  def getFloorOf(buildAddr: String): Future[Seq[Floor]] =
    db.run { floors.filter(_.buildAddr === buildAddr).sortBy(_.number).result }

  def getSwitches: Future[Seq[Switch]] = db.run { switches.result }

  def getSwitchesOfBuild(buildAddr: String): Future[Seq[Switch]] =
    db.run { switches.filter(_.buildAddr === buildAddr).result }

  def getSwitchesOfFloor(
    buildAddr: String,
    floorNumber: Int
  ): Future[Seq[Switch]] =
    db.run {
      switches
        .filter(_.buildAddr === buildAddr)
        .filter(_.floorNumber === floorNumber)
        .result
    }

  def createBuild(b: Build): Future[Int] = { db.run(builds += b) }

  def createFloor(f: Floor): Future[Int] = { db.run(floors += f) }

  def createSwitch(sw: Switch): Future[Int] = { db.run(switches += sw) }
}
