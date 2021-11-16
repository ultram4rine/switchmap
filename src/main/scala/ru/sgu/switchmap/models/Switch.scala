package ru.sgu.switchmap.models

final case class SwitchRequest(
  retrieveFromNetData: Boolean = false,
  retrieveUpLinkFromSeens: Boolean = false,
  retrieveTechDataFromSNMP: Boolean = false,
  name: String = "",
  ipResolveMethod: String = "",
  ip: Option[String] = None,
  mac: Option[String] = None,
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
  ip: String = "",
  mac: String = "",
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

final case class SavePositionRequest(
  top: Float = 0,
  left: Float = 0
)

final case class SwitchNotFound(name: String) extends Exception

final case class SeenRequest(
  MAC: String
)

final case class SeenResponse(
  Name: String,
  MAC: String,
  Switch: String,
  PortName: String,
  PortAlias: String,
  Metric: Int
)
