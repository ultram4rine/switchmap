package controllers.api

import forms.SwitchForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SwitchController @Inject() (
  cc: SwitchControllerComponents,
  switchResourceHandler: SwitchResourceHandler
)(implicit ec: ExecutionContext)
    extends SwitchBaseController(cc) {
  private val form: Form[SwitchForm] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "name" -> nonEmptyText,
        "vendor" -> nonEmptyText
      )(SwitchForm.apply)(SwitchForm.unapply)
    )
  }

  def switches: Action[AnyContent] =
    SwitchAction.async { implicit request =>
      switchResourceHandler.list.map { switches => Ok(Json.toJson(switches)) }
    }

  def switchesOfBuild(buildAddr: String): Action[AnyContent] =
    SwitchAction.async { implicit request =>
      switchResourceHandler.listOfBuild(buildAddr).map { switches =>
        Ok(Json.toJson(switches))
      }
    }

  def switchesOfFloor(buildAddr: String, floorNumber: Int): Action[AnyContent] =
    SwitchAction.async { implicit request =>
      switchResourceHandler.listOfFloor(buildAddr, floorNumber).map {
        switches =>
          Ok(Json.toJson(switches))
      }
    }

  /*
  private def processJsonSwitch[A]()(implicit
    request: SwitchRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[SwitchForm]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: SwitchForm) = {
      switchResourceHandler
        .create(input)
        .map { switch => Created(Json.toJson(switch)) }
    }

    form.bindFromRequest().fold(failure, success)
  }*/
}
