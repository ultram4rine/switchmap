package controllers.api

import forms.FloorForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FloorController @Inject() (
  cc: FloorControllerComponents,
  floorResourceHandler: FloorResourceHandler
)(implicit ec: ExecutionContext)
    extends FloorBaseController(cc) {

  private val form: Form[FloorForm] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "number" -> number,
        "buildName" -> nonEmptyText,
        "buildAddr" -> nonEmptyText
      )(FloorForm.apply)(FloorForm.unapply)
    )
  }

  def floorsOf(buildAddr: String): Action[AnyContent] =
    FloorAction.async { implicit request =>
      floorResourceHandler.listOf(buildAddr).map { floors =>
        Ok(Json.toJson(floors))
      }
    }

  def addFloor(): Action[AnyContent] =
    FloorAction.async { implicit request =>
      processJsonFloor()
    }

  private def processJsonFloor[A]()(implicit
    request: FloorRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[FloorForm]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: FloorForm) = {
      floorResourceHandler
        .create(input)
        .map { floor => Created(Json.toJson(floor)) }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
