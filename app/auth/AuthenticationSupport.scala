package auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.{ScalatraBase}
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

trait AuthenticationSupport extends ScalatraBase with ScentrySupport[User] {
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

  protected val scentryConfig: ScentryConfiguration =
    new ScentryConfig {}.asInstanceOf[ScentryConfiguration]

  override protected def configureScentry: Unit = {
    scentry.unauthenticated {
      scentry.strategies("Bearer").unauthenticated()
    }
  }

  override protected def registerAuthStrategies: Unit = {
    scentry.register("LDAP", app => new LDAPAuthStrategy(app))
    scentry.register("Bearer", app => new BearerAuthStrategy(app))
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
