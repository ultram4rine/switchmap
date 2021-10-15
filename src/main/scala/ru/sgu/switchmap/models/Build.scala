package ru.sgu.switchmap.models

final case class Build(name: String, shortName: String)

final case class BuildNotFound(shortName: String) extends Exception
