package controllers.api

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ApiRouter @Inject() (
  authController: AuthController,
  buildController: BuildController,
  floorController: FloorController,
  switchController: SwitchController
) extends SimpleRouter {
  override def routes: Routes = {
    case POST(p"/auth") =>
      authController.login

    case GET(p"/builds") =>
      buildController.builds

    case GET(p"/builds/$buildShortName") =>
      buildController.buildByShortName(buildShortName)

    case GET(p"/builds/$buildShortName/floors") =>
      floorController.floorsOf(buildShortName)

    case GET(p"/switches") =>
      switchController.switches

    case GET(p"/switches/$switchName") =>
      switchController.switchByName(switchName)

    case GET(p"/builds/$buildShortName/switches") =>
      switchController.switchesOfBuild(buildShortName)

    case GET(p"/builds/$buildShortName/$floorNumber/switches") =>
      switchController.switchesOfFloor(buildShortName, floorNumber.toInt)

    case POST(p"/builds") =>
      buildController.addBuild()

    case PUT(p"/builds/$buildShortName") =>
      buildController.updateBuild(buildShortName)

    case DELETE(p"/builds/$buildShortName") =>
      buildController.deleteBuild(buildShortName)

    case POST(p"/builds/$buildShortName/floors") =>
      floorController.addFloor(buildShortName)

    case DELETE(p"/builds/$buildShortName/$floorNumber") =>
      floorController.deleteFloor(buildShortName, floorNumber)

    case POST(p"/switches") =>
      switchController.addSwitch()

    case POST(p"/builds/$buildShortName/$floorNumber/switches") =>
      switchController.addSwitchToFloor(buildShortName, floorNumber)
  }
}
