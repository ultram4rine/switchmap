package controllers.api

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ApiRouter @Inject() (buildController: BuildController)
    extends SimpleRouter {
  val prefix = "/api/v2"

  override def routes: Routes = {
    case GET(p"/builds") =>
      buildController.builds

    case POST(p"/build") =>
      buildController.addBuild()
  }
}
