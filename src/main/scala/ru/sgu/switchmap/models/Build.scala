package ru.sgu.switchmap.models

import sttp.tapir.Schema.annotations._

@description("Build request")
final case class BuildRequest(
  @description("Name of build")
  @encodedExample("Building 1")
  name: String = "",
  @description("Short name of build. Used in URI path")
  @encodedExample("b1")
  shortName: String = ""
)

@description("Build response")
final case class BuildResponse(
  @description("Name of build")
  @encodedExample("Building 1")
  name: String = "",
  @description("Short name of build. Used in URI path")
  @encodedExample("b1")
  shortName: String = "",
  @description("Number of floors in this build")
  @encodedExample(1)
  floorsNumber: Long = -1,
  @description("Number of switches in this build")
  @encodedExample(2)
  switchesNumber: Long = -1
)

final case class BuildNotFound(shortName: String) extends Exception
