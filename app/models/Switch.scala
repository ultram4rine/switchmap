package models

import play.api.libs.json.{Json, OFormat}

case class Switch(
  name: String,
  ip: String,
  mac: String,
  vendor: String,
  revision: Option[String],
  serial: Option[String],
  upSwitch: Option[Int],
  port: Option[String],
  posTop: Int,
  posLeft: Int,
  buildAddr: String,
  floorNumber: Int
) {
  override def equals(that: Any): Boolean = false
}

object Switch {
  implicit val switchFormat: OFormat[Switch] = Json.format[Switch]
}
