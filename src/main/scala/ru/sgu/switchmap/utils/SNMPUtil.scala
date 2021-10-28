package ru.sgu.switchmap.utils

import java.util

import zio._
import ru.sgu.switchmap.config.AppConfig
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi._
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.{DefaultPDUFactory, TreeUtils}
import org.snmp4j.{CommunityTarget, PDU, Snmp}

import scala.concurrent.{ExecutionContext, Future}

object snmp {
  case class SwitchInfo(
    revision: String,
    serial: String
  )

  private val entPhysicalDescr = new OID(".1.3.6.1.2.1.47.1.1.1.1.2.1")
  private val entPhysicalSerialNum = new OID(".1.3.6.1.2.1.47.1.1.1.1.11.1")
  private val entPhysicalName = new OID(".1.3.6.1.2.1.47.1.1.1.1.7")

  type SNMPUtil = Has[SNMPUtil.Service]

  object SNMPUtil {
    trait Service {
      def getSwitchInfo(ip: String, community: String): Task[Option[SwitchInfo]]
    }

    val live: ULayer[SNMPUtil] =
      ZLayer.succeed(new Service {
        override def getSwitchInfo(
          ip: String,
          community: String
        ): Task[Option[SwitchInfo]] = {
          Task.fromFuture { implicit ec =>
            Future {
              val transport = new DefaultUdpTransportMapping()
              transport.listen()

              val target =
                new CommunityTarget(
                  new UdpAddress(s"${ip}/161"),
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
                    Some(SwitchInfo(revision.toString, serial.toString))
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
      })

    def getSwitchInfo(
      ip: String,
      community: String
    ): RIO[SNMPUtil, Option[SwitchInfo]] =
      ZIO.accessM(_.get.getSwitchInfo(ip, community))
  }
}
