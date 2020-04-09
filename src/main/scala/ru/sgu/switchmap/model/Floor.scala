package ru.sgu.switchmap.model

import slick.jdbc.PostgresProfile.api._

trait FloorComponent extends BuildComponent {
  class Floors(tag: Tag) extends Table[Floor](tag, "floors") {
    def number = column[Int]("number")

    def buildName = column[String]("build_name")
    def buildAddr = column[String]("build_addr")

    def * = (number, buildName, buildAddr) <> (Floor.tupled, Floor.unapply)

    def pk = primaryKey("floor_pk", (number, buildAddr))

    def build =
      foreignKey("build_fk", (buildName, buildAddr), builds)(
        b => (b.name, b.addr),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.Cascade
      )
  }

  val floors = TableQuery[Floors]
}

case class Floor(number: Int, buildName: String, buildAddr: String) {
  override def equals(that: Any): Boolean = false
}
