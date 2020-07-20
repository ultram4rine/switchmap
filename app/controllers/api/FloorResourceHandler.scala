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
    buildShortName: String,
    floorInput: FloorForm.Data
  )(implicit mc: MarkerContext): Future[Option[FloorResource]] = {
    dataRepository.getBuildByShortName(buildShortName).flatMap { maybeBuild =>
      maybeBuild.map { build =>
        val floor = Floor(floorInput.number, build.name, build.shortName)
        dataRepository.createFloor(floor).flatMap { _ =>
          createFloorResource(floor)
        }
      } match {
        case Some(r) => r.map(Some(_))
        case None    => Future.successful(None)
      }
    }
  }

  def delete(buildShortName: String, floorNumber: Int)(implicit
    mc: MarkerContext
  ): Future[Option[Int]] = {
    dataRepository
      .getFloorByShortNameAndNum(buildShortName, floorNumber)
      .flatMap { maybeFloor =>
        maybeFloor.map { floor =>
          dataRepository.deleteFloor(floor)
        } match {
          case Some(r) => r.map(Some(_))
          case None    => Future.successful(None)
        }
      }
  }

  def listOf(
    buildShortName: String
  )(implicit mc: MarkerContext): Future[Seq[FloorResource]] = {
    dataRepository.getFloorOf(buildShortName).flatMap { floors =>
      createFloorResourceSeq(floors)
    }
  }

  private def createFloorResource(f: Floor): Future[FloorResource] = {
    for {
      swNum <-
        dataRepository.getSwitchesOfFloor(f.buildShortName, f.number).map {
          _.size
        }
    } yield FloorResource(f.number, swNum)
  }

  private def createFloorResourceSeq(
    fSeq: Seq[Floor]
  ): Future[Seq[FloorResource]] = {
    Future.sequence(fSeq.map { f => createFloorResource(f) })
  }

}
