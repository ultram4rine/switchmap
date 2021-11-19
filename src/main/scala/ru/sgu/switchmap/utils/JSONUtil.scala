package ru.sgu.switchmap.utils

import io.circe.{Decoder, Encoder}
import inet.ipaddr.{IPAddress, IPAddressString, MACAddressString}
import inet.ipaddr.mac.MACAddress

import scala.util.Try

object JSONUtil {
  implicit val encodeIPAddress: Encoder[IPAddress] =
    Encoder.encodeString.contramap[IPAddress](_.toString)
  implicit val decodeIPAddress: Decoder[IPAddress] =
    Decoder.decodeString.emapTry { str =>
      Try(new IPAddressString(str).toAddress())
    }

  implicit val encodeMACAddress: Encoder[MACAddress] =
    Encoder.encodeString.contramap[MACAddress](_.toString)
  implicit val decodeMACAddress: Decoder[MACAddress] =
    Decoder.decodeString.emapTry { str =>
      Try(new MACAddressString(str).toAddress())
    }
}
