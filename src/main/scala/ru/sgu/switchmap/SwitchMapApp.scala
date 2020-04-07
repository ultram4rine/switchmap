package ru.sgu.switchmap

import org.scalatra.{FutureSupport, ScalatraServlet}
import ru.sgu.switchmap.{SwitchMapRoutes}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

class SwitchMapApp(val db: Database)
    extends ScalatraServlet
    with FutureSupport
    with SwitchMapRoutes {
  protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
