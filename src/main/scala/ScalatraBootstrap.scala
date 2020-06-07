import javax.servlet.ServletContext
import org.scalatra._
import org.slf4j.{Logger, LoggerFactory}
import ru.sgu.switchmap.SwitchMapApp
import ru.sgu.switchmap.model.{BuildComponent, FloorComponent, SwitchComponent}
import slick.jdbc.PostgresProfile.api._

class ScalatraBootstrap
    extends LifeCycle
    with BuildComponent
    with FloorComponent
    with SwitchComponent {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  val db = Database.forConfig("switchmap")

  override def init(context: ServletContext) {
    try {
      logger.info("Creating tables schemas")
      db.run(builds.schema.create)
      db.run(floors.schema.create)
      db.run(switches.schema.create)
    } catch {
      case ex: Exception => logger.error(ex.getMessage)
    }

    context.setInitParameter("org.scalatra.cors.allowCredentials", "false")

    context.mount(new SwitchMapApp(db), "/*")
  }

  private def closeDbConnection() {
    logger.info("Closing DB connection")
    db.close
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection()
  }
}
