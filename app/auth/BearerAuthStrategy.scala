package ru.sgu.switchmap.auth

import java.util.Locale

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.auth.ScentryStrategy
import org.scalatra.{ScalatraBase, Unauthorized}

import scala.util.{Failure, Success}

class BearerAuthStrategy(protected val app: ScalatraBase)(
  implicit request: HttpServletRequest,
  response: HttpServletResponse
) extends ScentryStrategy[User] {
  override def name: String = "Bearer"

  implicit def request2BearerAuthRequest(
    r: HttpServletRequest
  ): BearerAuthRequest = new BearerAuthRequest(r)

  protected def validate(userName: String, password: String): Option[User] = {
    if (userName == "admin" && password == "admin") Some(User(userName))
    else None
  }

  override def isValid(implicit request: HttpServletRequest): Boolean =
    request.isBearerAuth && request.providesAuth

  protected def validate(token: String): Option[User] = {
    val userTokenData = JWT.decodeUserJwt(token)
    userTokenData match {
      case Success(utd) => Some(User(utd.id))
      case Failure(_)   => None
    }
  }

  def authenticate()(
    implicit request: HttpServletRequest,
    response: HttpServletResponse
  ): Option[User] = validate(request.token)

  override def unauthenticated()(
    implicit request: HttpServletRequest,
    response: HttpServletResponse
  ) {
    app halt Unauthorized()
  }
}

class BearerAuthRequest(r: HttpServletRequest) {
  private val AUTHORIZATION_KEYS = List(
    "Authorization",
    "HTTP_AUTHORIZATION",
    "X-HTTP_AUTHORIZATION",
    "X_HTTP_AUTHORIZATION"
  )
  def parts: List[String] =
    authorizationKey map { r.getHeader(_).split(" ", 2).toList } getOrElse Nil
  def scheme: Option[String] =
    parts.headOption.map(sch => sch.toLowerCase(Locale.ENGLISH))
  def token: String = parts.lastOption getOrElse ""

  private def authorizationKey = AUTHORIZATION_KEYS.find(r.getHeader(_) != null)

  def isBearerAuth: Boolean =
    scheme.foldLeft(false) { (_, sch) => sch == "bearer" }
  def providesAuth: Boolean = authorizationKey.isDefined
}

case class User(id: String)
