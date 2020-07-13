package controllers.api

import forms.SwitchForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

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
        "mac" -> nonEmptyText,
        "snmpCommunity" -> nonEmptyText,
        "ipResolveMethod" -> nonEmptyText,
        "ip" -> optional(text)
      )(SwitchForm.apply)(SwitchForm.unapply)
    )
  }

  def addSwitch(): Action[AnyContent] =
    ApiAction.async { implicit request =>
      processJson4Create()
    }

  def addSwitchToFloor(
    buildShortName: String,
    floorNumber: String
  ): Action[AnyContent] =
    ApiAction.async { implicit request =>
      processJson4CreateWithLocation(buildShortName, floorNumber)
    }

  def switches: Action[AnyContent] =
    ApiAction.async { implicit request =>
      switchResourceHandler.list.map { switches => Ok(Json.toJson(switches)) }
    }

  def switchByName(switchName: String): Action[AnyContent] =
    ApiAction.async { implicit request =>
      switchResourceHandler.findByName(switchName).map {
        case Some(b) => Ok(Json.toJson(b))
        case None    => NoContent
      }
    }

  def switchesOfBuild(buildAddr: String): Action[AnyContent] =
    ApiAction.async { implicit request =>
      switchResourceHandler.listOfBuild(buildAddr).map { switches =>
        Ok(Json.toJson(switches))
      }
    }

  def switchesOfFloor(buildAddr: String, floorNumber: Int): Action[AnyContent] =
    ApiAction.async { implicit request =>
      switchResourceHandler.listOfFloor(buildAddr, floorNumber).map {
        switches =>
          Ok(Json.toJson(switches))
      }
    }

  private def processJson4Create[A]()(implicit
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
  }

  private def processJson4CreateWithLocation[A](
    buildShortName: String,
    floorNumber: String
  )(implicit request: ApiRequest[A]): Future[Result] = {
    def failure(badForm: Form[SwitchForm]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: SwitchForm) = {
      switchResourceHandler
        .createWithLocation(input, buildShortName, floorNumber)
        .map { switch => Created(Json.toJson(switch)) }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

case class SwitchControllerComponents @Inject() (
  apiActionBuilder: ApiActionBuilder,
  switchResourceHandler: SwitchResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: ExecutionContext
) extends ControllerComponents

class SwitchBaseController @Inject() (scc: SwitchControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = scc

  def ApiAction: ApiActionBuilder = scc.apiActionBuilder

  def switchResourceHandler: SwitchResourceHandler = scc.switchResourceHandler
}
