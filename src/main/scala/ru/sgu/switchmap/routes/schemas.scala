package ru.sgu.switchmap.routes

import inet.ipaddr.IPAddress
import inet.ipaddr.mac.MACAddress
import sttp.tapir.Schema

package object schemas {
  implicit val ipSchema: Schema[IPAddress] = Schema.string
  implicit val macSchema: Schema[MACAddress] = Schema.string
}
