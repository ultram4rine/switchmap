package ru.sgu.switchmap.models

final case class DBBuild(name: String = "", shortName: String = "")

final case class Build(
  name: String = "",
  shortName: String = "",
  floorsNumber: Long = -1,
  switchesNumber: Long = -1
)

final case class BuildNotFound(shortName: String) extends Exception
