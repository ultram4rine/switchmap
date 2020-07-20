package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, number}

object FloorForm {
  val form: Form[Data] = Form(
    mapping(
      "number" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(number: Int)
}
