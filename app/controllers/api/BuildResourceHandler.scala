package controllers.api

import forms.BuildForm
import javax.inject.Inject
import models.Build
import play.api.libs.json.{Format, Json}
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}

case class BuildResource(
  name: String,
  shortName: String,
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
    buildInput: BuildForm.Data
  ): Future[BuildResource] = {
    val build = Build(buildInput.name, buildInput.shortName)
    dataRepository.createBuild(build).flatMap { _ =>
      createBuildResource(build)
    }
  }

  def update(
    buildShortName: String,
    buildInput: BuildForm.Data
  ): Future[BuildResource] = {
    val build = Build(buildInput.name, buildInput.shortName)
    dataRepository.updateBuild(buildShortName, build).flatMap { _ =>
      createBuildResource(build)
    }
  }

  def delete(
    buildShortName: String
  ): Future[Option[Int]] = {
    dataRepository.getBuildByShortName(buildShortName).flatMap { maybeBuild =>
      maybeBuild.map { build =>
        dataRepository.deleteBuild(build)
      } match {
        case Some(r) => r.map(Some(_))
        case None    => Future.successful(None)
      }
    }
  }

  def list(): Future[Seq[BuildResource]] = {
    dataRepository.getBuilds.flatMap { builds =>
      val sortedBuilds =
        builds.sortBy(b => b.shortName.substring(1).toIntOption.getOrElse(1000))
      createBuildResourceSeq(sortedBuilds)
    }
  }

  def findByShortName(
    buildShortName: String
  ): Future[Option[BuildResource]] = {
    dataRepository.getBuildByShortName(buildShortName).flatMap { maybeBuild =>
      maybeBuild.map { build => createBuildResource(build) } match {
        case Some(b) => b.map(Some(_))
        case None    => Future.successful(None)
      }
    }
  }

  private def createBuildResource(b: Build): Future[BuildResource] = {
    for {
      fNum <- dataRepository.getFloorOf(b.shortName).map { _.size }
      swNum <- dataRepository.getSwitchesOfBuild(b.shortName).map { _.size }
    } yield BuildResource(b.name, b.shortName, fNum, swNum)
  }

  private def createBuildResourceSeq(
    bSeq: Seq[Build]
  ): Future[Seq[BuildResource]] = {
    Future.sequence(bSeq.map { b => createBuildResource(b) })
  }

}
