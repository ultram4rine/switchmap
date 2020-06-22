package controllers.api

import forms.BuildForm
import javax.inject.Inject
import models.Build
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class BuildResource(
  name: String,
  addr: String,
  floorsNumber: Int,
  switchesNumber: Int
)

object BuildResource {
  implicit val format: Format[BuildResource] = Json.format[BuildResource]
}

class BuildResourceHandler @Inject() (
  dataRepository: DataRepository
)(implicit ec: ExecutionContext) {

  def create(
    buildInput: BuildForm
  )(implicit mc: MarkerContext): Future[BuildResource] = {
    val build = Build(buildInput.name, buildInput.addr)
    dataRepository.createBuild(build).flatMap { _ =>
      createBuildResource(build)
    }
  }

  def list(implicit mc: MarkerContext): Future[Seq[BuildResource]] = {
    dataRepository.getBuilds.flatMap { builds =>
      val sortedBuilds =
        builds.sortBy(b => b.addr.substring(1).toIntOption.getOrElse(1000))
      createBuildResourceSeq(sortedBuilds)
    }
  }

  def findByAddr(
    buildAddr: String
  )(implicit mc: MarkerContext): Future[Option[BuildResource]] = {
    dataRepository.getBuildByAddr(buildAddr).flatMap { maybeBuild =>
      maybeBuild.map { build => createBuildResource(build) } match {
        case Some(b) => b.map(Some(_))
        case None    => Future.successful(None)
      }
    }
  }

  private def createBuildResource(b: Build): Future[BuildResource] = {
    for {
      fNum <- dataRepository.getFloorOf(b.addr).map { _.size }
      swNum <- dataRepository.getSwitchesOfBuild(b.addr).map { _.size }
    } yield BuildResource(b.name, b.addr, fNum, swNum)
  }

  private def createBuildResourceSeq(
    bSeq: Seq[Build]
  ): Future[Seq[BuildResource]] = {
    Future.sequence(bSeq.map { b => createBuildResource(b) })
  }

}
