package ru.sgu.switchmap.models

import sttp.tapir.Schema.annotations._

@description("Floor request")
final case class FloorRequest(
  @description("Number of floor")
  @encodedExample(1)
  number: Int = -1,
  @description("Short name of build of that floor")
  @encodedExample("b1")
  buildShortName: String = ""
)

@description("")
final case class FloorResponse(
  @description("Number of floor")
  @encodedExample(1)
  number: Int = -1,
  @description("Number of switches on that floor")
  @encodedExample(2)
  switchesNumber: Long = -1
)

final case class FloorNotFound(number: Int, buildShortName: String)
    extends Exception
