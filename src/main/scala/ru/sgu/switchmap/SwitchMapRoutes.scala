package ru.sgu.switchmap

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.CorsSupport
import org.scalatra.json._
import org.scalatra.{FutureSupport, ScalatraBase}
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile.api._
import ru.sgu.switchmap.model._

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
    db.run(
      builds.result
        .map(b =>
          b.sortWith((b1, b2) =>
            (b1.addr.substring(1).toInt < b2.addr.substring(1).toInt)
          )
        )
    )
  }

  get("/build/:addr/floors") {
    val addr = params("addr")

    db.run(floors.filter(_.buildAddr === addr).sortBy(_.number.asc).result)
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
