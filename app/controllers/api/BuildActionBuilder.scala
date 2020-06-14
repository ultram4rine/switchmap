package controllers.api

import javax.inject.Inject
import play.api.MarkerContext
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{
  ActionBuilder,
  AnyContent,
  BaseController,
  BodyParser,
  ControllerComponents,
  DefaultActionBuilder,
  MessagesRequestHeader,
  PlayBodyParsers,
  PreferredMessagesProvider,
  Request,
  Result,
  WrappedRequest
}

import scala.concurrent.{ExecutionContext, Future}

trait BuildRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider

class BuildRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with BuildRequestHeader

class BuildActionBuilder @Inject() (
  messagesApi: MessagesApi,
  playBodyParsers: PlayBodyParsers
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[BuildRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type PostRequestBlock[A] = BuildRequest[A] => Future[Result]

  override def invokeBlock[A](
    request: Request[A],
    block: PostRequestBlock[A]
  ): Future[Result] = {
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request
    )

    val future = block(new BuildRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }

}

case class BuildControllerComponents @Inject() (
  buildActionBuilder: BuildActionBuilder,
  buildResourceHandler: BuildResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents

class BuildBaseController @Inject() (bcc: BuildControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = bcc

  def BuildAction: BuildActionBuilder = bcc.buildActionBuilder

  def buildResourceHandler: BuildResourceHandler = bcc.buildResourceHandler
}
