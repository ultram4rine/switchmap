package controllers.api

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ApiRouter @Inject() (
  buildController: BuildController,
  floorController: FloorController
) extends SimpleRouter {
  val prefix = "/api/v2"

  override def routes: Routes = {
    case GET(p"/builds") =>
      buildController.builds

    case GET(p"/build/$buildAddr/floors") =>
      floorController.floorsOf(buildAddr)

    case POST(p"/build") =>
      buildController.addBuild()

    case POST(p"/floor") =>
      floorController.addFloor()
  }
}
