package controllers.api

import com.mohiva.play.silhouette.api.Silhouette
import forms.FloorForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi, MessagesProvider}
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FloorController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  cc: FloorControllerComponents,
  floorResourceHandler: FloorResourceHandler
)(implicit ec: ExecutionContext, provider: MessagesProvider)
    extends FloorBaseController(cc) {

  private val form = FloorForm.form

  private def failure(badForm: Form[FloorForm.Data]) = {
    Future.successful(BadRequest(badForm.errorsAsJson))
  }

  def addFloor(buildShortName: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          failure,
          input => {
            floorResourceHandler
              .create(buildShortName, input)
              .map { floor => Created(Json.toJson(floor)) }
          }
        )
    }

  def deleteFloor(buildAddr: String, floorNumber: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      floorNumber.toIntOption match {
        case Some(num) =>
          floorResourceHandler.delete(buildAddr, num).map {
            case Some(_) => Ok
            case None    => NoContent
          }
        case None => Future { BadRequest("wrong floor number") }
      }
    }

  def floorsOf(buildAddr: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      floorResourceHandler.listOf(buildAddr).map { floors =>
        Ok(Json.toJson(floors))
      }
    }

}

case class FloorControllerComponents @Inject() (
  floorResourceHandler: FloorResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: ExecutionContext
) extends ControllerComponents

class FloorBaseController @Inject() (fcc: FloorControllerComponents)
    extends BaseController
    with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = fcc
  def floorResourceHandler: FloorResourceHandler = fcc.floorResourceHandler

}
