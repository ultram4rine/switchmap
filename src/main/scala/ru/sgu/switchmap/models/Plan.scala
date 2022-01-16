package ru.sgu.switchmap.models

import sttp.model.Part
import sttp.tapir.TapirFile

final case class Plan(planFile: Part[TapirFile])
