package forms

case class SwitchForm(
  name: String,
  mac: String,
  snmpCommunity: String,
  ipResolveMethod: String,
  ip: Option[String]
)
