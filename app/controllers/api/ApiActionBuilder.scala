package controllers.api

import java.time.Clock

import auth.User
import javax.inject.Inject
import pdi.jwt.JwtSession._
import play.api.http.HttpVerbs
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import play.api.{Configuration, MarkerContext}

import scala.concurrent.{ExecutionContext, Future}

trait ApiRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider

class ApiRequest[A](
  val user: User,
  request: Request[A],
  val messagesApi: MessagesApi
) extends WrappedRequest[A](request)
    with ApiRequestHeader

class ApiActionBuilder @Inject() (
  messagesApi: MessagesApi,
  playBodyParsers: PlayBodyParsers
)(implicit val executionContext: ExecutionContext, configuration: Configuration)
    extends ActionBuilder[ApiRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  implicit val clock: Clock = Clock.systemUTC

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  override def invokeBlock[A](
    request: Request[A],
    block: ApiRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request
    )

    request.jwtSession.getAs[User]("user") match {
      case Some(user) =>
        block(new ApiRequest[A](user, request, messagesApi))
          .map(result => {
            result.refreshJwtSession(request)
            request.method match {
              case GET | HEAD =>
                result.withHeaders("Cache-Control" -> s"max-age: 100")
              case _ =>
                result
            }
          })
      case _ =>
        Future(Unauthorized)
    }
  }

}
