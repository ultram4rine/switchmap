package ru.sgu.switchmap.model

import slick.jdbc.PostgresProfile.api._

trait FloorComponent extends BuildComponent {
  case class Floor(id: Int, buildID: Int, number: Int) {
    override def equals(that: Any): Boolean = false
  }

  class Floors(tag: Tag) extends Table[Floor](tag, "floors") {
    def id = column[Int]("id", O.PrimaryKey)
    def buildID = column[Int]("build_id")
    def number = column[Int]("number")

    def * = (id, buildID, number) <> (Floor.tupled, Floor.unapply)

    def build = foreignKey("build_fk", buildID, builds)(_.id)
  }

  val floors = TableQuery[Floors]

  val findFloorsOfBuild = {
    for {
      f <- floors
      b <- f.build
    } yield (f.number, b.name)
  }
}
