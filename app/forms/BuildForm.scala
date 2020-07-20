package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object BuildForm {
  val form: Form[Data] = Form(
    mapping(
      "name" -> nonEmptyText,
      "shortName" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, shortName: String)
}
