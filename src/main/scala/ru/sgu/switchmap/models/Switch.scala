package ru.sgu.switchmap.models

final case class Switch(
  name: String,
  ip: String,
  mac: String,
  snmpCommunity: String,
  revision: Option[String],
  serial: Option[String],
  portsNumber: Option[Int],
  buildShortName: Option[String],
  floorNumber: Option[Int],
  positionTop: Option[Float],
  positionLeft: Option[Float],
  upSwitchName: Option[String],
  upSwitchMAC: Option[String],
  upLink: Option[String]
)

final case class SwitchNotFound(name: String) extends Exception
