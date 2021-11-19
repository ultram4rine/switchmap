package ru.sgu.switchmap.utils

import zio._
import inet.ipaddr.IPAddress
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi._
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.{CommunityTarget, PDU, Snmp}

import scala.concurrent.Future

case class SwitchInfo(
  revision: String,
  serial: String
)

trait SNMPUtil {
  def getSwitchInfo(ip: IPAddress, community: String): Task[Option[SwitchInfo]]
}

case class SNMPUtilLive() extends SNMPUtil {
  private val entPhysicalDescr = new OID(".1.3.6.1.2.1.47.1.1.1.1.2.1")
  private val entPhysicalSerialNum = new OID(".1.3.6.1.2.1.47.1.1.1.1.11.1")
  new OID(".1.3.6.1.2.1.47.1.1.1.1.7")

  override def getSwitchInfo(
    ip: IPAddress,
    community: String
  ): Task[Option[SwitchInfo]] = {
    Task.fromFuture { implicit ec =>
      Future {
        val transport = new DefaultUdpTransportMapping()
        transport.listen()

        val target =
          new CommunityTarget(
            new UdpAddress(s"${ip.toString()}/161"),
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
            responsePDU.getErrorIndex
            responsePDU.getErrorStatusText

            if (errorStatus == PDU.noError) {
              val revision = responsePDU.getVariable(entPhysicalDescr)
              val serial = responsePDU.getVariable(entPhysicalSerialNum)
              if (
                revision.toString() == "noSuchInstance" ||
                revision.toString() == "noSuchObject" ||
                serial.toString() == "noSuchInstance" ||
                serial.toString() == "noSuchObject"
              ) {
                None
              } else {
                Some(SwitchInfo(revision.toString, serial.toString))
              }
            } else {
              None
            }
          } else {
            None
          }
        } else {
          None
        }
      }
    }
  }
}

object SNMPUtilLive {
  val layer: ULayer[Has[SNMPUtil]] = SNMPUtilLive.layer
}

object SNMPUtil {
  def getSwitchInfo(
    ip: IPAddress,
    community: String
  ): RIO[Has[SNMPUtil], Option[SwitchInfo]] =
    ZIO.accessM(_.get.getSwitchInfo(ip, community))
}
