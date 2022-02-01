package ru.sgu.switchmap.utils

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import java.net.InetAddress
import ru.sgu.switchmap.config.AppConfig
import scala.concurrent.Future
import zio._

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
  val layer: RLayer[AppConfig, DNSUtil] = (DNSUtilLive(_)).toLayer
}

object DNSUtil {
  def getIPByHostname(name: String): RIO[DNSUtil, IPAddress] =
    ZIO.serviceWithZIO[DNSUtil](_.getIPByHostname(name))
}
