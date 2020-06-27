package models

import play.api.libs.json.{Json, OFormat}

case class Build(name: String, addr: String) {
  override def equals(that: Any): Boolean = false
}

object Build {
  def tupled: ((String, String)) => Build = (Build.apply _).tupled
  implicit val buildFormat: OFormat[Build] = Json.format[Build]
}
