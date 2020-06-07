package ru.sgu.switchmap.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.{ScalatraBase}
import org.scalatra.auth.strategy.BasicAuthSupport
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

trait AuthenticationSupport
    extends ScentrySupport[User]
    with BasicAuthSupport[User] {
  self: ScalatraBase =>

  protected def fromSession: PartialFunction[String, User] = {
    case id: String => User(id)
  }
  protected def toSession: PartialFunction[User, String] = {
    case usr: User => usr.id
  }

  protected def requireLogin(): Unit = {
    if (!isAuthenticated) {
      halt(401, "Unauthenticated")
    }
  }

  val realm = "Bearer Authentication"
  protected val scentryConfig: ScentryConfiguration =
    new ScentryConfig {}.asInstanceOf[ScentryConfiguration]

  override protected def configureScentry: Unit = {
    scentry.unauthenticated {
      scentry.strategies("Bearer").unauthenticated()
    }
  }

  override protected def registerAuthStrategies: Unit = {
    scentry.register("Bearer", app => new BearerAuthStrategy(app, realm))
  }

  protected def auth()(
    implicit
    request: HttpServletRequest,
    response: HttpServletResponse
  ): Option[User] = {
    val baReq = new BearerAuthRequest(request)
    if (!baReq.providesAuth) {
      halt(401, "Unauthenticated")
    }
    if (!baReq.isBearerAuth) {
      halt(400, "Bad Request")
    }
    scentry.authenticate("Bearer")
  }
}
