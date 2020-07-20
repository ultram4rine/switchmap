package controllers.api

import javax.inject.Inject
import play.api.MarkerContext
import play.api.http.HttpVerbs
import play.api.i18n.MessagesApi
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

trait ApiRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider

class ApiRequest[A](
  request: Request[A],
  val messagesApi: MessagesApi
) extends WrappedRequest[A](request)
    with ApiRequestHeader

class ApiActionBuilder @Inject() (
  messagesApi: MessagesApi,
  playBodyParsers: PlayBodyParsers
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[ApiRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  override def invokeBlock[A](
    request: Request[A],
    block: ApiRequest[A] => Future[Result]
  ): Future[Result] = {
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request
    )

    block(
      new ApiRequest[A](
        request,
        messagesApi
      )
    ).map(result => {
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case _ =>
          result
      }
    })
  }

}
