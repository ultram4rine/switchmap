package controllers.api

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject() (
  cc: ControllerComponents,
  buildResourceHandler: BuildResourceHandler
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def builds: Action[AnyContent] =
    Action.async { implicit request =>
      buildResourceHandler.find.map { builds =>
        Ok(Json.toJson(builds))
      }
    }
}
