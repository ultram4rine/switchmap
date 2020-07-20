package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}

object SwitchForm {
  val form: Form[Data] = Form(
    mapping(
      "name" -> nonEmptyText,
      "mac" -> nonEmptyText,
      "snmpCommunity" -> nonEmptyText,
      "ipResolveMethod" -> nonEmptyText,
      "ip" -> optional(text)
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    name: String,
    mac: String,
    snmpCommunity: String,
    ipResolveMethod: String,
    ip: Option[String]
  )
}
