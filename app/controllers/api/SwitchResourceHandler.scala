package controllers.api

import forms.SwitchForm
import javax.inject.Inject
import models.Switch
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}
import repositories.DataRepository
import utils.{DNSUtil, SNMPUtil}

import scala.concurrent.{ExecutionContext, Future}

case class SwitchResource(
  name: String,
  ip: Option[String],
  mac: String,
  revision: Option[String],
  serial: Option[String],
  portsNumber: Option[Int],
  buildShortName: Option[String],
  floorNumber: Option[Int],
  positionTop: Option[Int],
  positionLeft: Option[Int],
  upSwitchName: Option[String],
  upSwitchMAC: Option[String],
  upLink: Option[String]
)

object SwitchResource {
  implicit val format: Format[SwitchResource] = Json.format[SwitchResource]
}

class SwitchResourceHandler @Inject() (
  dataRepository: DataRepository,
  dnsUtil: DNSUtil,
  snmpUtil: SNMPUtil
)(implicit ec: ExecutionContext) {

  def create(
    switchInput: SwitchForm
  )(implicit mc: MarkerContext): Future[Option[SwitchResource]] = {
    val futureMaybeIP = switchInput.ipResolveMethod match {
      case "DNS"    => dnsUtil.getIPByHostname(switchInput.name)
      case "Direct" => Future { switchInput.ip }
      case _        => Future { None }
    }

    futureMaybeIP.flatMap {
      case Some(ip) =>
        snmpUtil.getSwitchInfo(ip, switchInput.snmpCommunityType).flatMap {
          switchInfo =>
            {
              val switch = Switch(
                switchInput.name,
                Some(ip),
                switchInput.mac,
                switchInfo.revision,
                switchInfo.serial,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None
              )
              dataRepository.createSwitch(switch).flatMap { _ =>
                Some(createSwitchResource(switch)) match {
                  case Some(sw) => sw.map(Some(_))
                }
              }
            }
        }
      case None => Future { None }
    }
  }

  def list(implicit mc: MarkerContext): Future[Seq[SwitchResource]] = {
    dataRepository.getSwitches.flatMap { switches =>
      createSwitchResourceSeq(switches)
    }
  }

  def findByName(
    switchName: String
  )(implicit mc: MarkerContext): Future[Option[SwitchResource]] = {
    dataRepository.getSwitchByName(switchName).flatMap { maybeSwitch =>
      maybeSwitch.map { switch => createSwitchResource(switch) } match {
        case Some(sw) => sw.map(Some(_))
        case None     => Future.successful(None)
      }
    }
  }

  def listOfBuild(
    buildAddr: String
  )(implicit mc: MarkerContext): Future[Seq[SwitchResource]] = {
    dataRepository.getSwitchesOfBuild(buildAddr).flatMap { switches =>
      createSwitchResourceSeq(switches)
    }
  }

  def listOfFloor(buildAddr: String, floorNumber: Int)(implicit
    mc: MarkerContext
  ): Future[Seq[SwitchResource]] = {
    dataRepository.getSwitchesOfFloor(buildAddr, floorNumber).flatMap {
      switches =>
        createSwitchResourceSeq(switches)
    }
  }

  private def createSwitchResource(sw: Switch): Future[SwitchResource] =
    Future {
      SwitchResource(
        sw.name,
        sw.ip,
        sw.mac,
        sw.revision,
        sw.serial,
        sw.portsNumber,
        sw.buildShortName,
        sw.floorNumber,
        sw.positionTop,
        sw.positionLeft,
        sw.upSwitchName,
        sw.upSwitchMAC,
        sw.upLink
      )
    }

  private def createSwitchResourceSeq(
    swSeq: Seq[Switch]
  ): Future[Seq[SwitchResource]] = {
    Future.sequence(swSeq.map { sw => createSwitchResource(sw) })
  }

}
