package controllers

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Mode}
import play.api.http.{ContentTypes, HttpErrorHandler}
import play.api.mvc._
import play.api.libs.ws.WSClient
import services.IndexRenderService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FrontendController @Inject() (
  cc: ControllerComponents,
  environment: Environment,
  ws: WSClient,
  indexRenderService: IndexRenderService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def vueApp(path: String): Action[AnyContent] =
    Action.async { implicit req =>
      environment.mode match {
        case Mode.Dev =>
          fetchWebpackServer(path)
        case _ =>
          renderIndexPage()
      }
    }

  private def fetchWebpackServer(
    path: String
  )(implicit request: RequestHeader) = {
    ws.url(s"http://localhost:8080/$path").get().map { r =>
      if (r.contentType.equalsIgnoreCase(HTML(Codec.utf_8))) {
        val html = r.bodyAsBytes.utf8String

        Ok(indexRenderService.setCsrfToken(html)).as(ContentTypes.HTML)
      } else {
        new Status(r.status)(r.bodyAsBytes).as(r.contentType)
      }
    }
  }

  private def renderIndexPage()(implicit request: RequestHeader) = {
    Future.successful {
      val html = indexRenderService.render(
        Some(
          "Scala PlayFramework authentication and user management sample using Silhouette VueJs"
        )
      )
      Ok(html).as(ContentTypes.HTML)
    }
  }
}
