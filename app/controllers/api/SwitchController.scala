package controllers.api

import com.mohiva.play.silhouette.api.Silhouette
import forms.SwitchForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SwitchController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  cc: SwitchControllerComponents,
  switchResourceHandler: SwitchResourceHandler
)(implicit ec: ExecutionContext)
    extends SwitchBaseController(cc) {

  private val form = SwitchForm.form

  private def failure[A](badForm: Form[SwitchForm.Data]) = {
    Future.successful(BadRequest(Json.toJson(badForm.errors)))
  }

  def addSwitch(): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          failure,
          input =>
            switchResourceHandler
              .create(input)
              .map { switch => Created(Json.toJson(switch)) }
        )
    }

  def addSwitchToFloor(
    buildShortName: String,
    floorNumber: String
  ): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          failure,
          input =>
            switchResourceHandler
              .createWithLocation(input, buildShortName, floorNumber)
              .map { switch => Created(Json.toJson(switch)) }
        )
    }

  def switches: Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      switchResourceHandler.list.map { switches => Ok(Json.toJson(switches)) }
    }

  def switchByName(switchName: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      switchResourceHandler.findByName(switchName).map {
        case Some(b) => Ok(Json.toJson(b))
        case None    => NoContent
      }
    }

  def switchesOfBuild(buildAddr: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      switchResourceHandler.listOfBuild(buildAddr).map { switches =>
        Ok(Json.toJson(switches))
      }
    }

  def switchesOfFloor(buildAddr: String, floorNumber: Int): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      switchResourceHandler.listOfFloor(buildAddr, floorNumber).map {
        switches =>
          Ok(Json.toJson(switches))
      }
    }

}

case class SwitchControllerComponents @Inject() (
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
  def switchResourceHandler: SwitchResourceHandler = scc.switchResourceHandler

}
