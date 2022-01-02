package ru.sgu.switchmap.models

import inet.ipaddr.{IPAddress, IPAddressString, MACAddressString}
import inet.ipaddr.mac.MACAddress

final case class SwitchRequest(
  retrieveFromNetData: Boolean = false,
  retrieveIPFromDNS: Boolean = false,
  retrieveUpLinkFromSeens: Boolean = false,
  retrieveTechDataFromSNMP: Boolean = false,
  name: String = "",
  ip: Option[IPAddress] = None,
  mac: Option[MACAddress] = None,
  snmpCommunity: String = "",
  revision: Option[String] = None,
  serial: Option[String] = None,
  buildShortName: Option[String] = None,
  floorNumber: Option[Int] = None,
  positionTop: Option[Float] = None,
  positionLeft: Option[Float] = None,
  upSwitchName: Option[String] = None,
  upLink: Option[String] = None
)

final case class SwitchResponse(
  name: String = "",
  ip: IPAddress,
  mac: MACAddress,
  revision: Option[String] = None,
  serial: Option[String] = None,
  buildShortName: Option[String] = None,
  floorNumber: Option[Int] = None,
  positionTop: Option[Float] = None,
  positionLeft: Option[Float] = None,
  upSwitchName: Option[String] = None,
  upLink: Option[String] = None
)

final case class SwitchPosition(
  name: String = "",
  top: Float = 0,
  left: Float = 0,
  moving: Boolean = true
)

final case class SwitchResult(
  sw: SwitchResponse,
  seen: Boolean = false,
  snmp: Boolean = false
)

final case class SavePositionRequest(
  top: Float = 0,
  left: Float = 0
)

final case class SwitchInfo(
  revision: String,
  serial: String
)

final case class LastSyncTime(
  syncTime: java.time.Instant,
  lock: String
)

final case class SwitchNotFound(name: String) extends Exception

final case class SeenRequest(
  MAC: MACAddress
)

final case class SeenResponse(
  Name: String,
  MAC: MACAddress,
  Switch: String,
  PortName: String,
  PortAlias: String,
  Metric: Int
)
