package ru.sgu.switchmap

import sttp.tapir.ztapir._
import zio.UIO

import ru.sgu.switchmap.auth.{Authorizer, AuthToken, AuthStatus}

package object routes {
  private[routes] val withAuth = endpoint
    .securityIn(auth.apiKey(header[Option[String]]("X-Auth-Token")))
    .errorOut(stringBody)
    .zServerSecurityLogic {
      case Some(token) =>
        Authorizer.authorize(AuthToken(token)).mapError(_.toString())
      case None => UIO.succeed(AuthStatus.Failed)
    }
}
