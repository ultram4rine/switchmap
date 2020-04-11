package ru.sgu.switchmap

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.CorsSupport
import org.scalatra.json._
import org.scalatra.{FutureSupport, ScalatraBase}
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile.api._
import ru.sgu.switchmap.model._
import org.scalatra.Conflict

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
        builds
          .map(b =>
            (
              b.name,
              b.addr,
              floors.filter(_.buildAddr === b.addr).map(_.number).length,
              switches.filter(_.buildAddr === b.addr).map(_.name).length
            )
          )
          .result
      )
      .map(_.groupBy { b =>
        BuildWithFloorsAndSwitchesCount(b._1, b._2, b._3, b._4)
      }.map(_._1))
  }

  get("/build/:addr/floors") {
    val addr = params("addr")

    db.run(
        floors
          .filter(_.buildAddr === addr)
          .map(f =>
            (
              f.number,
              switches
                .filter(s =>
                  (s.buildAddr === f.buildAddr && s.floorNumber === f.number)
                )
                .map(_.name)
                .length
            )
          )
          .result
      )
      .map(_.groupBy { f => FloorWithSwitchesCount(f._1, f._2) }.map(_._1))
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
      db.run(
        floors
          .filter(floor =>
            floor.number === f.number && floor.buildName === f.buildName && floor.buildAddr === f.buildAddr
          )
          .exists
          .result
          .flatMap { exists =>
            if (!exists) {
              floors += f
            } else {
              DBIO.successful(None)
            }
          }
      )
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

case class BuildWithFloorsAndSwitchesCount(
  name: String,
  addr: String,
  floors: Int,
  switches: Int
) {
  override def equals(that: Any): Boolean = false
}

case class FloorWithSwitchesCount(
  number: Int,
  switches: Int
) {
  override def equals(that: Any): Boolean = false
}
