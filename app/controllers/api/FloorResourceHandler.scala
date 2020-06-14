package controllers.api

import forms.FloorForm
import javax.inject.Inject
import models.Floor
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}

case class FloorResource(
  number: Int,
  switchesNumber: Int
)

object FloorResource {
  implicit val format: Format[FloorResource] = Json.format[FloorResource]
}

class FloorResourceHandler @Inject() (
  dataRepository: DataRepository
)(implicit ec: ExecutionContext) {

  def create(
    floorInput: FloorForm
  )(implicit mc: MarkerContext): Future[FloorResource] = {
    val floor =
      Floor(floorInput.number, floorInput.buildName, floorInput.buildAddr)
    dataRepository.createFloor(floor).flatMap { _ =>
      createFloorResource(floor)
    }
  }

  def listOf(
    buildAddr: String
  )(implicit mc: MarkerContext): Future[Seq[FloorResource]] = {
    dataRepository.getFloorOf(buildAddr).flatMap { floors =>
      createFloorResourceSeq(floors)
    }
  }

  private def createFloorResource(f: Floor): Future[FloorResource] = {
    for {
      swNum <-
        dataRepository.getSwitchesOfFloor(f.buildAddr, f.number).map { _.size }
    } yield FloorResource(f.number, swNum)
  }

  private def createFloorResourceSeq(
    fSeq: Seq[Floor]
  ): Future[Seq[FloorResource]] = {
    Future.sequence(fSeq.map { f => createFloorResource(f) })
  }

}
