package ru.sgu.switchmap.model

import slick.jdbc.PostgresProfile.api._

trait FloorComponent {
  lazy val builds = TableQuery[Build]

  class Floors(tag: Tag) extends Table[Floor](tag, "floors") {
    def id = column[Int]("id", O.PrimaryKey)
    def buildID = column[Int]("build_id")
    def number = column[Int]("number")

    def * = (id, buildID, number) <> (Floor.tupled, Floor.unapply)

    def build = foreignKey("build_fk", buildID, builds)(_.id)
  }
}

case class Floor(id: Int, buildID: Int, number: Int) {
  override def equals(that: Any): Boolean = false
}
