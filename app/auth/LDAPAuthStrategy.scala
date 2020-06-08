package ru.sgu.switchmap.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.{ScalatraBase, Unauthorized}
import org.scalatra.auth.ScentryStrategy

import scala.concurrent.{Await, ExecutionContext, Future}

class LDAPAuthStrategy(protected val app: ScalatraBase)(
  implicit request: HttpServletRequest,
  response: HttpServletResponse
) extends ScentryStrategy[User] {
  override def name: String = "LDAP"

  private def uid: String = app.params.getOrElse("username", "")
  private def password: String = app.params.getOrElse("password", "")

  override def isValid(implicit request: HttpServletRequest): Boolean = {
    uid != "" && password != ""
  }

  def authenticate()(
    implicit request: HttpServletRequest,
    response: HttpServletResponse
  ): Option[User] = {
    LDAPTools.authenticate(uid, password).flatMap {
      case Some(user) => Some(User(user.id))
      case None       => None
    }
  }

  override def unauthenticated()(
    implicit
    request: HttpServletRequest,
    response: HttpServletResponse
  ) {
    app halt Unauthorized()
  }
}
