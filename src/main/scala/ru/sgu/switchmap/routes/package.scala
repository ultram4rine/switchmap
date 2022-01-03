package ru.sgu.switchmap

import sttp.tapir.ztapir._
import zio.ZIO

import ru.sgu.switchmap.auth.{Authorizer, AuthToken, AuthStatus}

package object routes {
  private[routes] val secureEndpoint = endpoint
    .securityIn(auth.apiKey(header[Option[String]]("X-Auth-Token")))
    .errorOut(stringBody)
    .zServerSecurityLogic {
      case Some(token) =>
        Authorizer.authorize(AuthToken(token)).mapError(_.toString())
      case None => ZIO.fail("No token")
    }
}
