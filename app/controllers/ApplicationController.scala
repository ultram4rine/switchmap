package controllers

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import javax.inject.{Inject, Singleton}
import play.api.http.ContentTypes
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Environment, Mode}
import services.IndexRenderService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  environment: Environment,
  ws: WSClient,
  indexRenderService: IndexRenderService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def vueApp(path: String): Action[AnyContent] =
    silhouette.UserAwareAction.async { implicit req =>
      environment.mode match {
        case Mode.Dev =>
          fetchWebpackServer(path)
        case _ =>
          renderIndexPage()
      }
    }

  def signOut: Action[AnyContent] =
    silhouette.SecuredAction.async {
      implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
        silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
        silhouette.env.authenticatorService.discard(request.authenticator, Ok)
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
