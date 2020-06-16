package controllers.api

import forms.SwitchForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

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
    request: ApiRequest[A]
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

case class SwitchControllerComponents @Inject() (
  apiActionBuilder: ApiActionBuilder,
  switchResourceHandler: SwitchResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents

class SwitchBaseController @Inject() (scc: SwitchControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = scc

  def SwitchAction: ApiActionBuilder = scc.apiActionBuilder

  def switchResourceHandler: SwitchResourceHandler = scc.switchResourceHandler
}
