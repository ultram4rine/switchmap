package ru.sgu.switchmap.model

import slick.jdbc.PostgresProfile.api._

trait SwitchComponent extends BuildComponent with FloorComponent {
  case class Switch(
    id: Int,
    name: String,
    ip: String,
    mac: String,
    vendor: String,
    revision: Option[String],
    serial: Option[String],
    buildID: Int,
    floorID: Int,
    upSwitch: Option[Int],
    port: Option[String],
    posTop: Int,
    posLeft: Int
  ) {
    override def equals(that: Any): Boolean = false
  }

  class Switches(tag: Tag)
      extends Table[Switch](
        tag,
        "switches"
      ) {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def ip = column[String]("ip")
    def mac = column[String]("mac")
    def vendor = column[String]("vendor")
    def revision = column[Option[String]]("revision")
    def serial = column[Option[String]]("serial")
    def buildID = column[Int]("build_id")
    def floorID = column[Int]("floor_id")
    def upSwitch = column[Option[Int]]("switch_id")
    def port = column[Option[String]]("port")
    def posTop = column[Int]("pos_top")
    def posLeft = column[Int]("pos_left")

    def * =
      (
        id,
        name,
        ip,
        mac,
        vendor,
        revision,
        serial,
        buildID,
        floorID,
        upSwitch,
        port,
        posTop,
        posLeft
      ) <> (Switch.tupled, Switch.unapply)

    def build = foreignKey("build_fk", buildID, builds)(_.id)
    def floor = foreignKey("floor_fk", floorID, floors)(_.id)
  }

  val switches = TableQuery[Switches]
}