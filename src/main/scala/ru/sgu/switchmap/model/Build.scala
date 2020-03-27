package ru.sgu.switchmap.model

import slick.jdbc.PostgresProfile.api._

trait BuildComponent {
  lazy val Builds = TableQuery[Builds]

  class Builds {
    class Builds(tag: Tag) extends Table[Build](tag, "builds") {
      def id = column[Int]("id", O.PrimaryKey)
      def name = column[String]("name")
      def addr = column[String]("addr")

      def * = (id, name, addr) <> (Build.tupled, Build.unapply)
    }
  }
}

case class Build(id: Int, name: String, addr: String)
