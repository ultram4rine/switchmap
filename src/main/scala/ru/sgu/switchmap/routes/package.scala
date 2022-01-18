package ru.sgu.switchmap

import ru.sgu.switchmap.auth.Authorizer
import ru.sgu.switchmap.models.AuthToken
import sttp.model.StatusCode
import sttp.tapir.ztapir._

package object routes {
  private[routes] val secureEndpoint = endpoint
    .securityIn(auth.apiKey(header[String]("X-Auth-Token")))
    .errorOut(
      statusCode(StatusCode.Unauthorized).description("Unauthorized request")
    )
    .zServerSecurityLogic { token =>
      Authorizer.authorize(AuthToken(token)).mapError(_ => ())
    }
}
