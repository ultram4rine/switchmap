package ru.sgu.switchmap

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.CorsSupport
import org.scalatra.json._
import org.scalatra.{FutureSupport, ScalatraBase}
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile.api._
import ru.sgu.switchmap.model.{BuildComponent, FloorComponent, SwitchComponent}

trait SwitchMapRoutes
    extends ScalatraBase
    with FutureSupport
    with CorsSupport
    with JacksonJsonSupport
    with BuildComponent
    with FloorComponent
    with SwitchComponent {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  def db: Database

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  options("/*") {
    response.setHeader(
      "Access-Control-Allow-Origin",
      request.getHeader("Origin")
    )
  }

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

  post("/build") {
    var b: Build = parsedBody.extract[Build]

    try {
      db.run(DBIO.seq(builds += b))
    } catch {
      case ex: Exception => logger.error(ex.getMessage())
    }
  }

  post("/floor") {
    var f: Floor = parsedBody.extract[Floor]

    try {
      db.run(DBIO.seq(floors += f))
    } catch {
      case ex: Exception => logger.error(ex.getMessage())
    }
  }

  post("/switch") {
    var s: Switch = parsedBody.extract[Switch]

    try {
      db.run(DBIO.seq(switches += s))
    } catch {
      case ex: Exception => logger.error(ex.getMessage())
    }
  }
}
