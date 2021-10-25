package ru.sgu.switchmap.models

final case class SwitchRequest(
  retrieveFromNetData: Boolean = false,
  name: String = "",
  ipResolveMethod: String = "",
  ip: Option[String] = None,
  mac: Option[String] = None,
  snmpCommunity: String = "",
  revision: Option[String] = None,
  serial: Option[String] = None,
  portsNumber: Option[Int] = None,
  buildShortName: Option[String] = None,
  floorNumber: Option[Int] = None,
  positionTop: Option[Float] = None,
  positionLeft: Option[Float] = None,
  upSwitchName: Option[String] = None,
  upSwitchMAC: Option[String] = None,
  upLink: Option[String] = None
)

final case class SwitchResponse(
  name: String = "",
  ip: String = "",
  mac: String = "",
  revision: Option[String] = None,
  serial: Option[String] = None,
  portsNumber: Option[Int] = None,
  buildShortName: Option[String] = None,
  floorNumber: Option[Int] = None,
  positionTop: Option[Float] = None,
  positionLeft: Option[Float] = None,
  upSwitchName: Option[String] = None,
  upSwitchMAC: Option[String] = None,
  upLink: Option[String] = None
)

final case class SwitchNotFound(name: String) extends Exception
