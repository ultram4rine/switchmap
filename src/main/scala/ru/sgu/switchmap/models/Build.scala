package ru.sgu.switchmap.models

final case class BuildRequest(name: String = "", shortName: String = "")

final case class BuildResponse(
  name: String = "",
  shortName: String = "",
  floorsNumber: Long = -1,
  switchesNumber: Long = -1
)

final case class BuildNotFound(shortName: String) extends Exception
