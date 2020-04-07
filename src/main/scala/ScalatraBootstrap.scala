import javax.servlet.ServletContext
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}
import ru.sgu.switchmap.SwitchMapApp
import ru.sgu.switchmap.model.{BuildComponent, FloorComponent, SwitchComponent}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, Future}

class ScalatraBootstrap
    extends LifeCycle
    with BuildComponent
    with FloorComponent
    with SwitchComponent {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val db = Database.forConfig("switchmap")

  val createTables = DBIO.seq(
    (builds.schema ++ floors.schema ++ switches.schema).createIfNotExists,
    builds += (Build(1, "9 корпус", "b9"))
  )

  override def init(context: ServletContext) {
    try {
      db.run(createTables)
    } catch {
      case ex: Exception => logger.error(ex.getMessage())
    }

    context.mount(new SwitchMapApp(db), "/*")
  }

  private def closeDbConnection() {
    logger.info("Closing DB connection")
    db.close
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}
