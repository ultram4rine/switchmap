package utils

import java.net.InetAddress

import javax.inject.{Inject, Singleton}
import models.Switch
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DNSUtil @Inject() (configuration: Configuration)(implicit
  ec: ExecutionContext
) {
  private lazy val dnsSuffix =
    configuration.getOptional[String]("switchmap.dnsSuffix")

  def getIPByHostname(switch: Switch): Future[Option[String]] =
    Future {
      dnsSuffix match {
        case Some(suffix) =>
          val ip = InetAddress.getByName(s"${switch.name}.$suffix")
          Some(ip.getHostAddress)
        case None => None
      }
    }
}
