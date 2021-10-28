package ru.sgu.switchmap.utils

import java.net.InetAddress

import zio._
import ru.sgu.switchmap.config.AppConfig

import scala.concurrent.Future

object dns {
  type DNSUtil = Has[DNSUtil.Service]

  object DNSUtil {
    trait Service {
      def getIPByHostname(name: String): Task[String]
    }

    val live: ZLayer[Has[AppConfig], Nothing, DNSUtil] =
      ZLayer.fromService { cfg =>
        new Service {
          override def getIPByHostname(name: String): Task[String] = {
            Task.fromFuture { implicit ec =>
              Future {
                val ip = InetAddress.getByName(s"${name}.${cfg.dnsSuffix}")
                ip.getHostAddress
              }
            }
          }
        }
      }

    def getIPByHostname(name: String): RIO[DNSUtil, String] =
      ZIO.accessM(_.get.getIPByHostname(name))
  }
}
