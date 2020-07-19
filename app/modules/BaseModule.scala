package modules

import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{BruteForceDefenderActor, BruteForceDefenderConf}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[BruteForceDefenderActor]("brute-force-defender")
  }

  @Provides
  def providesBruteForceDefenderConf(
    conf: Configuration
  ): BruteForceDefenderConf = {
    val attempts = conf.getOptional[Int]("signin.limit.attempts").getOrElse(5)
    val period = conf
      .getOptional[FiniteDuration]("signin.limit.period")
      .getOrElse(30.minutes)
    BruteForceDefenderConf(attempts, period)
  }
}
