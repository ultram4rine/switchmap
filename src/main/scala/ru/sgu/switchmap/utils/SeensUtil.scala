package ru.sgu.switchmap.utils

import io.circe.Decoder
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import zio._
import zio.interop.catz
import zio.interop.catz._
import org.http4s.blaze.client.BlazeClientBuilder
import ru.sgu.switchmap.models.{SeenRequest, SeenResponse}
import org.http4s.Uri
import org.http4s.Method.POST
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._

import scala.concurrent.ExecutionContext.global
import cats.effect.kernel.Resource
import scala.util.matching.Regex
import ru.sgu.switchmap.config.AppConfig
import org.http4s.Request
import org.http4s.EntityDecoder

object seens {
  type SeensUtil = Has[SeensUtil.Service]

  private implicit val zioRuntime: zio.Runtime[zio.ZEnv] =
    zio.Runtime.default

  private implicit val dispatcher: cats.effect.std.Dispatcher[zio.Task] =
    zioRuntime
      .unsafeRun(
        cats.effect.std
          .Dispatcher[zio.Task]
          .allocated
      )
      ._1

  private val reg: Regex = "(.{2})".r
  private def macDenormalized(mac: String): String =
    reg.replaceAllIn(mac, m => s"${m}:").dropRight(1)

  object SeensUtil {
    implicit def circeJsonDecoder[A](implicit
      decoder: Decoder[A]
    ): EntityDecoder[Task, A] = jsonOf[Task, A]

    trait Service {
      def get(mac: String): Task[Option[SeenResponse]]
    }

    val live: ZLayer[Has[AppConfig], Nothing, SeensUtil] =
      ZLayer.fromService { cfg =>
        new Service {
          override def get(mac: String): Task[Option[SeenResponse]] = {
            val dsl: Http4sClientDsl[Task] = new Http4sClientDsl[Task] {}
            import dsl._

            val uri = Uri.fromString(
              s"${cfg.seensHost}/mac"
            )
            uri match {
              case Right(value) => {
                {
                  val req: Request[Task] = POST(
                    SeenRequest(macDenormalized(mac)).asJson,
                    value
                  )
                  for {
                    seensAll <- BlazeClientBuilder[Task]
                      .withExecutionContext(global)
                      .resource
                      .use { client =>
                        client.expect[List[SeenResponse]](req)
                      }
                    seensNow = seensAll.filter(seen =>
                      seen.Name.contains("(now)")
                    )
                    seens = if (seensNow.isEmpty) seensAll else seensNow
                  } yield seens.sortBy(seen => seen.Metric).headOption
                }
              }
              case Left(value) =>
                Task.fail(new Exception("invalid URI for seens"))
            }
          }
        }
      }

    def get(mac: String): RIO[SeensUtil, Option[SeenResponse]] =
      ZIO.accessM(_.get.get(mac))
  }
}
