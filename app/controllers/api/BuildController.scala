package controllers.api

import com.mohiva.play.silhouette.api.Silhouette
import forms.BuildForm
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi, MessagesProvider}
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BuildController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  cc: BuildControllerComponents,
  buildResourceHandler: BuildResourceHandler
)(implicit ec: ExecutionContext, provider: MessagesProvider)
    extends BuildBaseController(cc) {

  private val form = BuildForm.form

  private def failure[A](badForm: Form[BuildForm.Data]) = {
    Future.successful(BadRequest(badForm.errorsAsJson))
  }

  def addBuild(): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          failure,
          input => {
            buildResourceHandler
              .create(input)
              .map { build => Created(Json.toJson(build)) }
          }
        )
    }

  def updateBuild(buildShortName: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          failure,
          input => {
            buildResourceHandler
              .update(buildShortName, input)
              .map { build => Created(Json.toJson(build)) }
          }
        )
    }

  def deleteBuild(buildAddr: String): Action[AnyContent] = {
    silhouette.SecuredAction.async { implicit request =>
      buildResourceHandler.delete(buildAddr).map {
        case Some(_) => Ok
        case None    => NoContent
      }
    }
  }

  def builds: Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      buildResourceHandler.list().map { builds =>
        Ok(Json.toJson(builds))
      }
    }

  def buildByShortName(buildAddr: String): Action[AnyContent] =
    silhouette.SecuredAction.async { implicit request =>
      buildResourceHandler.findByShortName(buildAddr).map {
        case Some(b) => Ok(Json.toJson(b))
        case None    => NoContent
      }
    }

}

case class BuildControllerComponents @Inject() (
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
  def buildResourceHandler: BuildResourceHandler = bcc.buildResourceHandler

}
