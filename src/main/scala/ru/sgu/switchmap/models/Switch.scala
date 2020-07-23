package ru.sgu.switchmap.models

case class Switch(
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

case object SwitchNotFoundError
