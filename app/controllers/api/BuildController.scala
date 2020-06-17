package controllers.api

import forms.BuildForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BuildController @Inject() (
  cc: BuildControllerComponents,
  buildResourceHandler: BuildResourceHandler
)(implicit ec: ExecutionContext)
    extends BuildBaseController(cc) {

  private val form: Form[BuildForm] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "name" -> nonEmptyText,
        "addr" -> nonEmptyText
      )(BuildForm.apply)(BuildForm.unapply)
    )
  }

  def builds: Action[AnyContent] =
    ApiAction.async { implicit request =>
      buildResourceHandler.find.map { builds =>
        Ok(Json.toJson(builds))
      }
    }

  def addBuild(): Action[AnyContent] =
    ApiAction.async { implicit request =>
      processJsonBuild()
    }

  private def processJsonBuild[A]()(implicit
    request: ApiRequest[A]
  ): Future[Result] = {
    def failure(badForm: Form[BuildForm]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: BuildForm) = {
      buildResourceHandler
        .create(input)
        .map { build => Created(Json.toJson(build)) }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

case class BuildControllerComponents @Inject() (
  apiActionBuilder: ApiActionBuilder,
  buildResourceHandler: BuildResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: ExecutionContext
) extends ControllerComponents

class BuildBaseController @Inject() (bcc: BuildControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = bcc

  def ApiAction: ApiActionBuilder = bcc.apiActionBuilder

  def buildResourceHandler: BuildResourceHandler = bcc.buildResourceHandler
}
