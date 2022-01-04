package ru.sgu.switchmap.models

import inet.ipaddr.{IPAddress, IPAddressString, MACAddressString}
import inet.ipaddr.mac.MACAddress
import sttp.tapir.Schema.annotations._

@description("Switch request")
final case class SwitchRequest(
  @description("Retrieve data from NetData service")
  @encodedExample(true)
  retrieveFromNetData: Boolean = false,
  @description("Retrieve IP from DNS")
  @encodedExample(true)
  retrieveIPFromDNS: Boolean = false,
  @description("Retrieve uplink from Seens service")
  @encodedExample(true)
  retrieveUpLinkFromSeens: Boolean = false,
  @description("Retrieve technical data from via SNMP")
  @encodedExample(true)
  retrieveTechDataFromSNMP: Boolean = false,
  @description("Name of switch")
  @encodedExample("switch1")
  name: String = "",
  @description(
    "IP of switch. Required only if both `retrieveFromNetData` and `retrieveIPFromDNS` is set to `false`"
  )
  @encodedExample("192.168.1.1")
  ip: Option[IPAddress] = None,
  @description(
    "MAC of switch. Required only if `retrieveFromNetData` is set to `false`"
  )
  @encodedExample("00:11:22:33:44:55")
  mac: Option[MACAddress] = None,
  @description(
    "SNMP community. Required only if `retrieveTechDataFromSNMP` is set to `true`"
  )
  @encodedExample("private")
  snmpCommunity: String = "",
  @description(
    "Revision of switch. Required only if `retrieveTechDataFromSNMP` is set to `false`"
  )
  @encodedExample("Vendor Model")
  revision: Option[String] = None,
  @description(
    "Serial number of switch. Required only if `retrieveTechDataFromSNMP` is set to `false`"
  )
  @encodedExample("AA000BB111")
  serial: Option[String] = None,
  @description("Build of switch")
  @encodedExample("b1")
  buildShortName: Option[String] = None,
  @description("Floor number of switch")
  @encodedExample(1)
  floorNumber: Option[Int] = None,
  @description("Y-axis position of switch on plan")
  @encodedExample(322.193)
  positionTop: Option[Float] = None,
  @description("X-axis position of switch on plan")
  @encodedExample(521.855)
  positionLeft: Option[Float] = None,
  @description(
    "Name of upswitch. Required only if `retrieveUpLinkFromSeens` is set to `false`"
  )
  @encodedExample("switch2")
  upSwitchName: Option[String] = None,
  @description(
    "Port name on upswitch. Required only if `retrieveUpLinkFromSeens` is set to `false`"
  )
  @encodedExample("48")
  upLink: Option[String] = None
)

@description("Switch response")
final case class SwitchResponse(
  @description("Name of switch")
  @encodedExample("switch1")
  name: String = "",
  @description("IP of switch")
  @encodedExample("192.168.1.1")
  ip: IPAddress,
  @description("MAC of switch")
  @encodedExample("00:11:22:33:44:55")
  mac: MACAddress,
  @description("Revision of switch")
  @encodedExample("Vendor Model")
  revision: Option[String] = None,
  @description("Serial number of switch")
  @encodedExample("AA000BB111")
  serial: Option[String] = None,
  @description("Build of switch")
  @encodedExample("b1")
  buildShortName: Option[String] = None,
  @description("Floor number of switch")
  @encodedExample(1)
  floorNumber: Option[Int] = None,
  @description("Y-axis position of switch on plan")
  @encodedExample(322.193)
  positionTop: Option[Float] = None,
  @description("X-axis position of switch on plan")
  @encodedExample(521.855)
  positionLeft: Option[Float] = None,
  @description("Name of upswitch")
  @encodedExample("switch2")
  upSwitchName: Option[String] = None,
  @description("Port name on upswitch")
  @encodedExample("48")
  upLink: Option[String] = None
)

@description("Result of adding/updating switch")
final case class SwitchResult(
  @description("Added/Updated switch")
  sw: SwitchResponse,
  @description("Result of retrieving uplink from Seens service")
  @encodedExample(true)
  seen: Boolean = false,
  @description("Result of retrieving technical data via SNMP")
  @encodedExample(true)
  snmp: Boolean = false
)

@description("Requet for updating position of switch on plan")
final case class SavePositionRequest(
  @description("Y-axis position of switch on plan")
  @encodedExample(322.193)
  top: Float = 0,
  @description("X-axis position of switch on plan")
  @encodedExample(521.855)
  left: Float = 0
)

final case class SwitchInfo(
  revision: String,
  serial: String
)

/* final case class SyncResult(

) */

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
