package controllers.api

import forms.FloorForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

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
    ApiAction.async { implicit request =>
      floorResourceHandler.listOf(buildAddr).map { floors =>
        Ok(Json.toJson(floors))
      }
    }

  def addFloor(): Action[AnyContent] =
    ApiAction.async { implicit request =>
      processJsonFloor()
    }

  def deleteFloor(buildAddr: String, floorNumber: String): Action[AnyContent] =
    ApiAction.async { implicit request =>
      floorNumber.toIntOption match {
        case Some(num) =>
          floorResourceHandler.delete(buildAddr, num).map {
            case Some(_) => Ok
            case None    => NoContent
          }
        case None => Future { BadRequest() }
      }
    }

  private def processJsonFloor[A]()(implicit
    request: ApiRequest[A]
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

case class FloorControllerComponents @Inject() (
  apiActionBuilder: ApiActionBuilder,
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

  def ApiAction: ApiActionBuilder = fcc.apiActionBuilder

  def floorResourceHandler: FloorResourceHandler = fcc.floorResourceHandler
}
