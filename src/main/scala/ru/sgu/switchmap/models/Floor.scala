package ru.sgu.switchmap.models

final case class FloorRequest(
  number: Int = -1,
  buildName: String = "",
  buildShortName: String = ""
)

final case class FloorResponse(number: Int = -1, switchesNumber: Long = -1)

final case class FloorNotFound(number: Int, buildShortName: String)
    extends Exception
