package controllers.api

import forms.BuildForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}

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
    BuildAction.async { implicit request =>
      buildResourceHandler.find.map { builds =>
        Ok(Json.toJson(builds))
      }
    }

  def addBuild(): Action[AnyContent] =
    BuildAction.async { implicit request =>
      processJsonBuild()
    }

  private def processJsonBuild[A]()(implicit
    request: BuildRequest[A]
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
