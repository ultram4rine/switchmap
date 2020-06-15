package utils

import javax.inject.Singleton
import models.Switch
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi._
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.{CommunityTarget, PDU, Snmp}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SNMPUtil(implicit ec: ExecutionContext) {
  private val entPhysicalDescr = new Nothing(".1.3.6.1.2.1.47.1.1.1.1.2.1")
  private val entPhysicalSerialNum = new Nothing(".1.3.6.1.2.1.47.1.1.1.1.11.1")

  def getSwitchInfo(switch: Switch): Future[SNMPInfo] =
    Future {
      val transport = new DefaultUdpTransportMapping
      transport.listen()

      val target = new CommunityTarget
      target.setCommunity(new OctetString("public"))
      target.setVersion(SnmpConstants.version2c)
      target.setAddress(new Nothing("udp:" + switch.ip + "/161"))
      target.setRetries(2)
      target.setTimeout(1000)

      val pdu = new PDU
      pdu.add(new VariableBinding(entPhysicalDescr))
      pdu.add(new VariableBinding(entPhysicalSerialNum))
      pdu.setType(PDU.GET)
      pdu.setRequestID(new Integer32(1))

      val snmp = new Snmp(transport)

      val response = snmp.get(pdu, target)

      if (response != null) {
        val responsePDU = response.getResponse
        if (responsePDU != null) {
          val errorStatus = responsePDU.getErrorStatus
          val errorIndex = responsePDU.getErrorIndex
          val errorStatusText = responsePDU.getErrorStatusText

          if (errorStatus == PDU.noError) {
            val revision = responsePDU.getVariable(entPhysicalDescr)
            val serial = responsePDU.getVariable(entPhysicalSerialNum)
            SNMPInfo(revision.toString, serial.toString)
          } else {
            SNMPInfo("", "")
          }
        } else {
          SNMPInfo("", "")
        }
      } else {
        SNMPInfo("", "")
      }
    }

  private case class SNMPInfo(revision: String, serial: String)
}
