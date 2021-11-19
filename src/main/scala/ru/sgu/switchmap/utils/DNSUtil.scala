package ru.sgu.switchmap.utils

import java.net.InetAddress

import zio._
import inet.ipaddr.IPAddress
import ru.sgu.switchmap.config.AppConfig

import scala.concurrent.Future
import inet.ipaddr.IPAddressString

object dns {
  type DNSUtil = Has[DNSUtil.Service]

  object DNSUtil {
    trait Service {
      def getIPByHostname(name: String): Task[IPAddress]
    }

    val live: ZLayer[Has[AppConfig], Nothing, DNSUtil] =
      ZLayer.fromService { cfg =>
        new Service {
          override def getIPByHostname(name: String): Task[IPAddress] = {
            Task.fromFuture { implicit ec =>
              Future {
                val ip = InetAddress.getByName(s"${name}.${cfg.dnsSuffix}")
                new IPAddressString(ip.getHostAddress()).toAddress()
              }
            }
          }
        }
      }

    def getIPByHostname(name: String): RIO[DNSUtil, IPAddress] =
      ZIO.accessM(_.get.getIPByHostname(name))
  }
}
