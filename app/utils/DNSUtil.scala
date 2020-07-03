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

  def getIPByHostname(switchName: String): Future[Option[String]] =
    Future {
      dnsSuffix match {
        case Some(suffix) =>
          val ip = InetAddress.getByName(s"$switchName.$suffix")
          Some(ip.getHostAddress)
        case None => None
      }
    }
}
