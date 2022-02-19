package ru.sgu.switchmap.routes

import java.nio.file.Paths
import ru.sgu.switchmap.auth.{Authorizer, AuthStatus}
import ru.sgu.switchmap.models.Plan
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir._
import zio._
import zio.stream.{Stream, ZSink, ZStream}

final case class PlanRoutes[R <: Authorizer with Any]() {
  type PlanTask[A] = RIO[R, A]

  private[this] val planBaseEndpoint = secureEndpoint.tag("plans")

  val getPlanEndpoint = planBaseEndpoint.get
    .in("plans" / path[String]("planName"))
    .out(streamBinaryBody(ZioStreams))
    .serverLogic { as => planName =>
      ZIO.succeed(
        ZStream
          .fromPath(Paths.get(s"./plans/${planName}"))
      )
    }
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
                  .fromPath(Paths.get(s"./plans/${shortName}f${number}.png"))
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
