package forms

case class SwitchForm(
  name: String,
  ipResolveMethod: String,
  ip: Option[String],
  mac: String,
  snmpCommunityType: String,
  snmpCommunity: Option[String],
  buildAddr: Option[String],
  floorNumber: Option[Int]
)
