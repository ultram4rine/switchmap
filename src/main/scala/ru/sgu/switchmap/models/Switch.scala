package ru.sgu.switchmap.models

final case class Switch(
  name: String = "",
  ip: String = "",
  mac: String = "",
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

final case class SwitchNotFound(name: String) extends Exception
