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
  val prefix = "/api/v2"

  override def routes: Routes = {
    case POST(p"/auth") =>
      authController.login

    case GET(p"/builds") =>
      buildController.builds

    case GET(p"/build/$buildShortName") =>
      buildController.buildByAddr(buildShortName)

    case GET(p"/build/$buildShortName/floors") =>
      floorController.floorsOf(buildShortName)

    case GET(p"/switches") =>
      switchController.switches

    case GET(p"/switch/$switchName") =>
      switchController.switchByName(switchName)

    case GET(p"/build/$buildShortName/switches") =>
      switchController.switchesOfBuild(buildShortName)

    case GET(p"/build/$buildShortName/$floorNumber/switches") =>
      switchController.switchesOfFloor(buildShortName, floorNumber.toInt)

    case POST(p"/build") =>
      buildController.addBuild()

    case PUT(p"/build/$buildShortName") =>
      buildController.updateBuild(buildShortName)

    case DELETE(p"/build/$buildShortName") =>
      buildController.deleteBuild(buildShortName)

    case POST(p"/floor") =>
      floorController.addFloor()

    case DELETE(p"/build/$buildShortName/$floorNumber") =>
      floorController.deleteFloor(buildShortName, floorNumber)

    case POST(p"/switch") =>
      switchController.addSwitch()
  }
}
