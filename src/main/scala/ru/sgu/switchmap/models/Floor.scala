package ru.sgu.switchmap.models

final case class DBFloor(number: Int, buildName: String, buildShortName: String)

final case class Floor(number: Int, switchesNumber: Long)

final case class FloorNotFound(number: Int, buildShortName: String)
    extends Exception
