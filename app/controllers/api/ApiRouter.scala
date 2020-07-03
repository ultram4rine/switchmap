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

    case GET(p"/build/$buildAddr") =>
      buildController.buildByAddr(buildAddr)

    case GET(p"/build/$buildAddr/floors") =>
      floorController.floorsOf(buildAddr)

    case GET(p"/switches") =>
      switchController.switches

    case GET(p"/switch/$switchName") =>
      switchController.switchByName(switchName)

    case GET(p"/build/$buildAddr/switches") =>
      switchController.switchesOfBuild(buildAddr)

    case GET(p"/build/$buildAddr/$floorNumber/switches") =>
      switchController.switchesOfFloor(buildAddr, floorNumber.toInt)

    case POST(p"/build") =>
      buildController.addBuild()

    case PUT(p"/build/$buildAddr") =>
      buildController.updateBuild(buildAddr)

    case DELETE(p"/build/$buildAddr") =>
      buildController.deleteBuild(buildAddr)

    case POST(p"/floor") =>
      floorController.addFloor()

    case DELETE(p"/build/$buildAddr/$floorNumber") =>
      floorController.deleteFloor(buildAddr, floorNumber)

    case POST(p"/switch") =>
      switchController.addSwitch()
  }
}
