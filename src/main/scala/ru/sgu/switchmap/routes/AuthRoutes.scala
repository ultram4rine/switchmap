package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{EntityDecoder, EntityEncoder, Challenge}
import org.http4s.rho.RhoRoutes
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._

import ru.sgu.switchmap.auth.{Authenticator, AuthToken}
import ru.sgu.switchmap.models.User

final case class AuthRoutes[R <: Has[Authenticator]]() {
  type AuthTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[AuthTask, A] = jsonOf[AuthTask, A]
  implicit def circeJsonEncoder[A](implicit
    decoder: Encoder[A]
  ): EntityEncoder[AuthTask, A] =
    jsonEncoderOf[AuthTask, A]

  var authEndpoint = endpoint.post
    .in("auth" / "login")
    .in(jsonBody[User])
    .errorOut(stringBody)
    .out(jsonBody[AuthToken])
    .zServerLogic { user =>
      Authenticator
        .authenticate(user.username, user.password, user.rememberMe)
        .mapError(_.toString())
    }

  val routes = ZHttp4sServerInterpreter()
    .from(authEndpoint)
    .toRoutes

  val api: RhoRoutes[AuthTask] =
    new RhoRoutes[AuthTask] {
      val swaggerIO = org.http4s.rho.swagger.SwaggerSupport[AuthTask]
      import swaggerIO._

      "Authenticates user" **
        POST / "auth" / "login" ^ jsonOf[AuthTask, User] |>> { (user: User) =>
          Authenticator
            .authenticate(user.username, user.password, user.rememberMe)
            .foldM(
              _ =>
                Unauthorized(
                  `WWW-Authenticate`(
                    Challenge(
                      "X-Auth-Token",
                      "SwitchMap",
                      Map.empty
                    )
                  )
                ),
              Ok(_)
            )
        }
    }
}
