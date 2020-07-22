package ru.sgu.switchmap.utils

import java.net.InetAddress

import scala.concurrent.{ExecutionContext, Future}

class DNSUtil(implicit
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
