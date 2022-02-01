package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import ru.sgu.switchmap.auth.Authenticator
import ru.sgu.switchmap.models.{AuthToken, User}
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio._

final case class AuthRoutes[R <: Authenticator]() {
  val authEndpoint: ZServerEndpoint[R, Any] = endpoint
    .tag("auth")
    .summary("Authenticates user")
    .post
    .in("auth" / "login")
    .in(jsonBody[User])
    .errorOut(
      statusCode.description(StatusCode.Unauthorized, "Invalid credentials")
    )
    .out(jsonBody[AuthToken])
    .zServerLogic { user =>
      Authenticator
        .authenticate(user.username, user.password, user.rememberMe)
        .mapError(_ => StatusCode.Unauthorized)
    }

  val routes = List(authEndpoint)
}
