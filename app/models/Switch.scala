package models

import play.api.libs.json.{Json, OFormat}

case class Switch(
  name: String,
  ip: Option[String],
  mac: String,
  revision: Option[String],
  serial: Option[String],
  portsNumber: Option[Int],
  buildShortName: Option[String],
  floorNumber: Option[Int],
  positionTop: Option[Int],
  positionLeft: Option[Int],
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
      Option[String],
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
