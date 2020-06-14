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

trait FloorRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider

class FloorRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with FloorRequestHeader

class FloorActionBuilder @Inject() (
  messagesApi: MessagesApi,
  playBodyParsers: PlayBodyParsers
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[FloorRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type PostRequestBlock[A] = FloorRequest[A] => Future[Result]

  override def invokeBlock[A](
    request: Request[A],
    block: PostRequestBlock[A]
  ): Future[Result] = {
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request
    )

    val future = block(new FloorRequest(request, messagesApi))

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

case class FloorControllerComponents @Inject() (
  floorActionBuilder: FloorActionBuilder,
  floorResourceHandler: FloorResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents

class FloorBaseController @Inject() (fcc: FloorControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = fcc

  def FloorAction: FloorActionBuilder = fcc.floorActionBuilder

  def floorResourceHandler: FloorResourceHandler = fcc.floorResourceHandler
}
