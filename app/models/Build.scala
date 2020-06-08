package models

import slick.jdbc.PostgresProfile.api._

trait BuildComponent {
  class Builds(tag: Tag) extends Table[Build](tag, "builds") {
    def name = column[String]("name", O.Unique)
    def addr = column[String]("addr", O.Unique)

    def * = (name, addr) <> (Build.tupled, Build.unapply)

    def pk = primaryKey("build_pk", (name, addr))
  }

  val builds = TableQuery[Builds]
}

case class Build(name: String, addr: String) {
  override def equals(that: Any): Boolean = false
}
