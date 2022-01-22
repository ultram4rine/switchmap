package ru.sgu.switchmap

import ru.sgu.switchmap.auth.Authorizer
import ru.sgu.switchmap.models.AuthToken
import sttp.model.StatusCode
import sttp.tapir.ztapir._

package object routes {
  private val logic = (token: String) =>
    Authorizer.authorize(AuthToken(token)).mapError(_ => ())
  private val status =
    statusCode(StatusCode.Unauthorized).description("Unauthorized request")

  private[routes] val secureEndpoint = endpoint
    .securityIn(auth.apiKey(header[String]("X-Auth-Token")))
    .errorOut(status)
    .zServerSecurityLogic(logic)

  private[routes] val secureCookieEndpoint = endpoint
    .securityIn(auth.apiKey(cookie[String]("X-Auth-Token")))
    .errorOut(status)
    .zServerSecurityLogic(logic)
}
