package ru.sgu.switchmap.models

final case class DBFloor(
  number: Int = -1,
  buildName: String = "",
  buildShortName: String = ""
)

final case class Floor(number: Int = -1, switchesNumber: Long = -1)

final case class FloorNotFound(number: Int, buildShortName: String)
    extends Exception
