package models

import play.api.libs.json.{Json, OFormat}

case class Floor(number: Int, buildName: String, buildAddr: String) {
  override def equals(that: Any): Boolean = false
}

object Floor {
  implicit val floorFormat: OFormat[Floor] = Json.format[Floor]
}
