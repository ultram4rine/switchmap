package ru.sgu.switchmap.routes

import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.{Request, Response, StaticFile, Challenge}
import org.http4s.multipart.Multipart
import org.http4s.EntityDecoder
import ru.sgu.switchmap.auth.{Authorizer, AuthStatus}
import ru.sgu.switchmap.models.Plan
import ru.sgu.switchmap.Main.AppTask
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._
import zio.stream.{Stream, ZSink}
import zio.blocking.Blocking
import java.nio.file.Paths
import scala.language.existentials
import sttp.capabilities.zio.ZioStreams
import zio.stream.ZStream
import ru.sgu.switchmap.models.AuthToken
import sttp.tapir.static.Files

final case class PlanRoutes[R <: Has[Authorizer] with Blocking]() {
  type PlanTask[A] = RIO[R, A]

  private[this] val planBaseEndpoint = secureEndpoint.tag("plans")

  val getPlanEndpoint = filesGetServerEndpoint[PlanTask]("plans")("./plans")
    .tag("plans")
    .summary("Get plan")
  val uploadPlanEndpoint = planBaseEndpoint
    .summary("Upload plan")
    .post
    .in("plans" / path[String]("shortName") / path[Int]("number"))
    .in(multipartBody[Plan])
    .out(stringBody)
    .serverLogic { as =>
      { case (shortName, number, plan) =>
        as match {
          case AuthStatus.Succeed =>
            val stream = Stream
              .fromInputStream(
                new java.io.FileInputStream(plan.planFile.body)
              )
              .run(
                ZSink
                  .fromFile(Paths.get(s"./plans/${shortName}f${number}.png"))
              )
            stream
              .mapError(_ => ())
              .map(_ => "Plan saved")
          case _ => ZIO.fail(())
        }
      }
    }

  val routes = List(
    getPlanEndpoint.widen[R],
    uploadPlanEndpoint.widen[R]
  )
}
