package ru.sgu.switchmap.utils

import io.circe.Decoder
import org.http4s.client.dsl.Http4sClientDsl
import zio._
import zio.interop.catz._
import org.http4s.ember.client.EmberClientBuilder
import ru.sgu.switchmap.models.{SeenRequest, SeenResponse}
import org.http4s.Uri
import org.http4s.Method.POST
import inet.ipaddr.mac.MACAddress
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._

import scala.concurrent.ExecutionContext.global
import scala.util.matching.Regex
import ru.sgu.switchmap.config.AppConfig
import org.http4s.Request
import org.http4s.EntityDecoder

trait SeensUtil {
  def getSeenOf(mac: MACAddress): Task[Option[SeenResponse]]
}

case class SeensUtilLive(cfg: AppConfig) extends SeensUtil {
  private implicit val zioRuntime: zio.Runtime[zio.ZEnv] =
    zio.Runtime.default

  import ru.sgu.switchmap.json._

  implicit def circeJsonDecoder[A](implicit
    decoder: Decoder[A]
  ): EntityDecoder[Task, A] = jsonOf[Task, A]

  override def getSeenOf(mac: MACAddress): Task[Option[SeenResponse]] = {
    val dsl: Http4sClientDsl[Task] = new Http4sClientDsl[Task] {}
    import dsl._

    val req: Request[Task] =
      POST(SeenRequest(mac).asJson, cfg.seensHost / "mac")
    for {
      seensAll <- EmberClientBuilder
        .default[Task]
        .build
        .use { client =>
          client.expect[List[SeenResponse]](req)
        }
      seensNow = seensAll.filter(seen => seen.Name.contains("(now)"))
      seens = if (seensNow.isEmpty) seensAll else seensNow
    } yield seens.sortBy(seen => seen.Metric).headOption
  }
}

object SeensUtilLive {
  val layer: RLayer[Has[AppConfig], Has[SeensUtil]] = (SeensUtilLive(_)).toLayer
}

object SeensUtil {
  def getSeenOf(mac: MACAddress): RIO[Has[SeensUtil], Option[SeenResponse]] =
    ZIO.serviceWith[SeensUtil](_.getSeenOf(mac))
}
