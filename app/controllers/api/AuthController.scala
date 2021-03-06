package controllers.api

import java.time.Clock

import auth.ldap.LDAP
import javax.inject.{Inject, Singleton}
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Configuration
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject() (
  cc: AuthControllerComponents,
  ldap: LDAP
)(implicit
  ec: ExecutionContext,
  configuration: Configuration
) extends AuthBaseController(cc) {

  implicit val clock: Clock = Clock.systemUTC

  private val loginForm: Reads[(String, String)] =
    ((JsPath \ "username").read[String] and (JsPath \ "password")
      .read[String]).tupled

  def login: Action[JsValue] =
    Action(parse.json).async { implicit request =>
      request.body
        .validate(loginForm)
        .fold(
          errors => {
            Future { BadRequest(JsError.toJson(errors)) }
          },
          {
            case (username, password) =>
              ldap.bind(username, password).map {
                case 0 =>
                  Ok(
                    JwtJson.encode(
                      Json.obj(("user", 1)),
                      configuration.get[String]("play.http.secret.key"),
                      JwtAlgorithm.HS256
                    )
                  )
                case _ => Unauthorized
              }
          }
        )
    }
}

case class AuthControllerComponents @Inject() (
  apiActionBuilder: ApiActionBuilder,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: ExecutionContext
) extends ControllerComponents

class AuthBaseController @Inject() (acc: AuthControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = acc

  def ApiAction: ApiActionBuilder = acc.apiActionBuilder
}
