package models

import play.api.libs.json.{Json, OFormat}

case class Floor(number: Int, buildName: String, buildAddr: String) {
  override def equals(that: Any): Boolean = false
}

object Floor {
  def tupled: ((Int, String, String)) => Floor = (Floor.apply _).tupled
  implicit val floorFormat: OFormat[Floor] = Json.format[Floor]
}
