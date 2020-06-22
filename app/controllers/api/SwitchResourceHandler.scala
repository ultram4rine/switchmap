package controllers.api

import javax.inject.Inject
import models.Switch
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}

case class SwitchResource(
  name: String,
  ip: String,
  mac: String,
  vendor: String,
  revision: Option[String],
  serial: Option[String],
  upSwitch: Option[Int],
  port: Option[String],
  posTop: Int,
  posLeft: Int,
  buildAddr: String,
  floorNumber: Int
)

object SwitchResource {
  implicit val format: Format[SwitchResource] = Json.format[SwitchResource]
}

class SwitchResourceHandler @Inject() (
  dataRepository: DataRepository
)(implicit ec: ExecutionContext) {

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
        sw.vendor,
        sw.revision,
        sw.serial,
        sw.upSwitch,
        sw.port,
        sw.posTop,
        sw.posLeft,
        sw.buildAddr,
        sw.floorNumber
      )
    }

  private def createSwitchResourceSeq(
    swSeq: Seq[Switch]
  ): Future[Seq[SwitchResource]] = {
    Future.sequence(swSeq.map { sw => createSwitchResource(sw) })
  }

}
