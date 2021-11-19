package ru.sgu.switchmap.utils

import java.net.InetAddress

import zio._
import inet.ipaddr.IPAddress
import ru.sgu.switchmap.config.AppConfig

import scala.concurrent.Future
import inet.ipaddr.IPAddressString

trait DNSUtil {
  def getIPByHostname(name: String): Task[IPAddress]
}

case class DNSUtilLive(cfg: AppConfig) extends DNSUtil {
  override def getIPByHostname(name: String): Task[IPAddress] = {
    Task.fromFuture { implicit ec =>
      Future {
        val ip = InetAddress.getByName(s"${name}.${cfg.dnsSuffix}")
        new IPAddressString(ip.getHostAddress()).toAddress()
      }
    }
  }
}

object DNSUtilLive {
  val layer: RLayer[Has[AppConfig], Has[DNSUtil]] = (DNSUtilLive(_)).toLayer
}

object DNSUtil {
  def getIPByHostname(name: String): RIO[Has[DNSUtil], IPAddress] =
    ZIO.accessM(_.get.getIPByHostname(name))
}
