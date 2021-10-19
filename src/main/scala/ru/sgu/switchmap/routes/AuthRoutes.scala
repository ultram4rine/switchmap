package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Challenge}
import org.http4s.rho.RhoRoutes
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

  val api: RhoRoutes[AuthTask] =
    new RhoRoutes[AuthTask] {
      val swaggerIO = org.http4s.rho.swagger.SwaggerSupport[AuthTask]
      import swaggerIO._

      "Authenticates user" **
        POST / "login" ^ jsonOf[AuthTask, User] |>> { (user: User) =>
        Authenticator
          .authenticate(user.username, user.password)
          .foldM(
            _ =>
              Unauthorized(
                `WWW-Authenticate`(
                  Challenge(
                    "Authorization: Bearer",
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
