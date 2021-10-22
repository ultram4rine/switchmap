package ru.sgu.switchmap.utils

import java.util

import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi._
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.{DefaultPDUFactory, TreeUtils}
import org.snmp4j.{CommunityTarget, PDU, Snmp}

import scala.concurrent.{ExecutionContext, Future}

class SNMPUtil(implicit ec: ExecutionContext) {
  private val entPhysicalDescr = new OID(".1.3.6.1.2.1.47.1.1.1.1.2.1")
  private val entPhysicalSerialNum = new OID(".1.3.6.1.2.1.47.1.1.1.1.11.1")
  private val entPhysicalName = new OID(".1.3.6.1.2.1.47.1.1.1.1.7")

  // TODO: Split this methods in one
  def getSwitchInfo(
    ip: String,
    community: String
  ): Future[SwitchInfo] =
    Future {
      val transport = new DefaultUdpTransportMapping()
      transport.listen()

      val target =
        new CommunityTarget(
          new UdpAddress(s"$ip/161"),
          new OctetString(community)
        )
      target.setVersion(SnmpConstants.version2c)
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
            SwitchInfo(Some(revision.toString), Some(serial.toString))
          } else {
            SwitchInfo(None, None)
          }
        } else {
          SwitchInfo(None, None)
        }
      } else {
        SwitchInfo(None, None)
      }
    }

  def getSwitchPortsNumber(
    ip: String,
    community: String
  ): Future[Int] =
    Future {
      val transport = new DefaultUdpTransportMapping()
      transport.listen()

      val target =
        new CommunityTarget(
          new UdpAddress(s"$ip/161"),
          new OctetString(community)
        )
      target.setVersion(SnmpConstants.version2c)
      target.setRetries(2)
      target.setTimeout(1000)

      val snmp = new Snmp(transport)

      val result = new util.TreeMap[String, String]

      val treeUtils = new TreeUtils(snmp, new DefaultPDUFactory)
      val events = treeUtils.getSubtree(target, entPhysicalName)

      events.forEach { event =>
        val varBindings = event.getVariableBindings
        if (varBindings == null || varBindings.isEmpty) {
          snmp.close()
          return Future { 0 }
        }
        for (varBinding <- varBindings) {
          if (varBinding == null) {
            snmp.close()
            return Future { 0 }
          }
          result.put(
            "." + varBinding.getOid.toString,
            varBinding.getVariable.toString
          )
        }
      }

      snmp.close()

      var portsCount = 0
      result.forEach { (_, v) =>
        if (v.contains("Port ")) {
          portsCount += 1
        }
      }
      portsCount
    }

  case class SwitchInfo(revision: Option[String], serial: Option[String])
}
