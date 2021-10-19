package ru.sgu.switchmap.routes

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Challenge}
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

  private def unauthenticated =
    IO.succeed(Left(new Exception("bad format authentication")))

  val dsl: Http4sDsl[AuthTask] = Http4sDsl[AuthTask]
  import dsl._

  def route: HttpRoutes[AuthTask] = {
    HttpRoutes.of[AuthTask] {
      case req @ POST -> Root / "login" =>
        req.decode[User] { user =>
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
}
