package models

import slick.jdbc.PostgresProfile.api._

trait SwitchComponent extends BuildComponent with FloorComponent {
  class Switches(tag: Tag)
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
      ) <> (Switch.tupled, Switch.unapply)

    def pk = primaryKey("switch_pk", (name, mac))

    def floor =
      foreignKey("floor_fk", (floorNumber, buildAddr), floors)(
        f => (f.number, f.buildAddr),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.Cascade
      )
  }

  val switches = TableQuery[Switches]
}

case class Switch(
  name: String,
  ip: String,
  mac: String,
  vendor: String,
  revision: Option[String],
  serial: Option[String],
  upSwitch: Option[Int],
  port: Option[String],
  posTop: Int,
  posLeft: Int,
  buildAddr: String,
  floorNumber: Int
) {
  override def equals(that: Any): Boolean = false
}
