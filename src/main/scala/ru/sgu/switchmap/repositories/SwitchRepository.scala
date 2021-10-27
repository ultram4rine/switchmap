package ru.sgu.switchmap.repositories

import io.grpc.Status
import doobie.implicits._
import doobie.{Query0, Update0}
import doobie.hikari.HikariTransactor
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import ru.sgu.git.netdataserv.netdataproto.{
  GetNetworkSwitchesResponse,
  NetworkSwitch,
  GetMatchingHostRequest,
  Match,
  StaticHost
}
import ru.sgu.git.netdataserv.netdataproto.ZioNetdataproto.NetDataClient
import ru.sgu.switchmap.db.DBTransactor
import ru.sgu.switchmap.config.AppConfig
import ru.sgu.switchmap.models.{SwitchRequest, SwitchResponse, SwitchNotFound}
import ru.sgu.switchmap.utils.seens.SeensUtil

object SwitchRepository {

  trait Service {
    def snmp(): Task[List[String]]
    def get(): Task[List[SwitchResponse]]
    def getOf(build: String): Task[List[SwitchResponse]]
    def getOf(build: String, floor: Int): Task[List[SwitchResponse]]
    def get(name: String): Task[SwitchResponse]
    def create(switch: SwitchRequest): Task[Boolean]
    def update(name: String, switch: SwitchRequest): Task[Boolean]
    def delete(name: String): Task[Boolean]
  }

  val live: URLayer[DBTransactor with Has[
    AppConfig
  ] with NetDataClient with SeensUtil, SwitchRepository] =
    ZLayer.fromServices[
      DBTransactor.Resource,
      AppConfig,
      NetDataClient.Service,
      SeensUtil.Service,
      SwitchRepository.Service
    ] { (resource, cfg, ndc, seensClient) =>
      DoobieSwitchRepository(resource.xa, cfg, ndc, seensClient)
    }
}

private[repositories] final case class DoobieSwitchRepository(
  xa: HikariTransactor[Task],
  cfg: AppConfig,
  ndc: NetDataClient.Service,
  seensClient: SeensUtil.Service
) extends SwitchRepository.Service {

  import Tables.ctx._

  def snmp(): Task[List[String]] = {
    Task.succeed(cfg.snmpCommunities)
  }

  def get(): Task[List[SwitchResponse]] = {
    val q = quote {
      Tables.switches.sortBy(sw => sw.name)
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def getOf(build: String): Task[List[SwitchResponse]] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.buildShortName.getOrNull == lift(build))
        .sortBy(sw => sw.name)
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def getOf(build: String, floor: Int): Task[List[SwitchResponse]] = {
    val q = quote {
      Tables.switches
        .filter(sw =>
          sw.buildShortName.getOrNull == lift(
            build
          ) && sw.floorNumber.getOrNull == lift(floor)
        )
        .sortBy(sw => sw.name)
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        switches => Task.succeed(switches)
      )
  }

  def get(
    name: String
  ): Task[SwitchResponse] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .map(_.headOption)
      .foldM(
        err => Task.fail(err),
        maybeSwitch =>
          Task.require(SwitchNotFound(name))(Task.succeed(maybeSwitch))
      )
  }

  def create(switch: SwitchRequest): Task[Boolean] = {
    val sw: IO[Object, SwitchResponse] = switch.retrieveFromNetData match {
      case true => {
        for {
          resp <- ndc.getMatchingHost(
            GetMatchingHostRequest(
              Some(Match(Match.Match.HostName(switch.name)))
            )
          )
          maybeSwitch = resp.host.headOption
          sw <- maybeSwitch match {
            case Some(value) =>
              IO.succeed(
                SwitchResponse(
                  value.name,
                  value.ipv4Address.mkString("."),
                  value.macAddressString,
                  buildShortName = switch.buildShortName,
                  floorNumber = switch.floorNumber
                )
              )
            case None => IO.fail(new Exception("no such switch"))
          }
        } yield sw
      }
      case false =>
        IO.succeed(
          SwitchResponse(
            switch.name,
            switch.ip.getOrElse(""),
            switch.mac.getOrElse(""),
            buildShortName = switch.buildShortName,
            floorNumber = switch.floorNumber
          )
        )
    }

    val withSeens = sw
      .flatMap { s =>
        for {
          seens <- seensClient.get(s.mac).catchAll(e => UIO.succeed(List()))
          seen = seens.headOption
          newSw = seen match {
            case None => s
            case Some(value) =>
              s.copy(
                upSwitchName = Some(value.Switch),
                upLink = Some(value.PortName)
              )
          }
        } yield newSw
      }

    withSeens
      .flatMap { s =>
        {
          val q = quote {
            Tables.switches.insert(lift(s))
          }

          Tables.ctx
            .run(q)
            .transact(xa)
            .foldM(err => Task.fail(err), _ => Task.succeed(true))
        }
      }
      .mapError(s => new Exception(s.toString()))
  }

  def update(
    name: String,
    switch: SwitchRequest
  ): Task[Boolean] = {
    val s = SwitchResponse(
      switch.name,
      switch.ip.getOrElse(""),
      switch.mac.getOrElse("")
    )
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
        .update(lift(s))
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(true))
  }

  def delete(name: String): Task[Boolean] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
        .delete
    }

    Tables.ctx
      .run(q)
      .transact(xa)
      .foldM(err => Task.fail(err), _ => Task.succeed(true))
  }

}
