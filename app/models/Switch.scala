package models

import play.api.libs.json.{Json, OFormat}

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
) {
  override def equals(that: Any): Boolean = false
}

object Switch {
  def tupled: (
    (
      String,
      String,
      String,
      String,
      Option[String],
      Option[String],
      Option[Int],
      Option[String],
      Option[Int],
      Option[Int],
      Option[Int],
      Option[String],
      Option[String],
      Option[String]
    )
  ) => Switch = (Switch.apply _).tupled
  implicit val switchFormat: OFormat[Switch] = Json.format[Switch]
}
