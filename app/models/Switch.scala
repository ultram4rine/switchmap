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
  buildShortName: String,
  floorNumber: Int
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
      Int,
      Int,
      String,
      Int
    )
  ) => Switch = (Switch.apply _).tupled
  implicit val switchFormat: OFormat[Switch] = Json.format[Switch]
}
