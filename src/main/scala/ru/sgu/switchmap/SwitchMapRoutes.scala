package ru.sgu.switchmap

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.scalatra.{FutureSupport, ScalatraBase}
import slick.jdbc.PostgresProfile.api._
import ru.sgu.switchmap.model.{BuildComponent, FloorComponent, SwitchComponent}

trait SwitchMapRoutes
    extends ScalatraBase
    with FutureSupport
    with JacksonJsonSupport
    with BuildComponent
    with FloorComponent
    with SwitchComponent {
  def db: Database

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/builds") {
    db.run(builds.sortBy(_.addr.desc).result)
  }

  get("/floors") {
    db.run(floors.sortBy(_.number.desc).result)
  }

  get("/switches") {
    db.run(switches.sortBy(_.name.desc).result)
  }
}
