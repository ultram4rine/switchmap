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
import ru.sgu.switchmap.models.{
  SwitchRequest,
  SwitchResponse,
  SavePositionRequest,
  SwitchNotFound
}
import ru.sgu.switchmap.utils.seens.SeensUtil
import ru.sgu.switchmap.utils.dns.DNSUtil
import ru.sgu.switchmap.utils.snmp.{SNMPUtil, SwitchInfo}

object SwitchRepository {

  trait Service {
    def snmp(): Task[List[String]]
    def get(): Task[List[SwitchResponse]]
    def getOf(build: String): Task[List[SwitchResponse]]
    def getOf(build: String, floor: Int): Task[List[SwitchResponse]]
    def get(name: String): Task[SwitchResponse]
    def create(switch: SwitchRequest): Task[Boolean]
    def update(name: String, switch: SwitchRequest): Task[Boolean]
    def update(name: String, position: SavePositionRequest): Task[Boolean]
    def delete(name: String): Task[Boolean]
  }

  val live: URLayer[DBTransactor with Has[
    AppConfig
  ] with NetDataClient with SeensUtil with DNSUtil with SNMPUtil, SwitchRepository] =
    ZLayer.fromServices[
      DBTransactor.Resource,
      AppConfig,
      NetDataClient.Service,
      SeensUtil.Service,
      DNSUtil.Service,
      SNMPUtil.Service,
      SwitchRepository.Service
    ] { (resource, cfg, ndc, seensClient, dns, snmp) =>
      DoobieSwitchRepository(resource.xa, cfg, ndc, seensClient, dns, snmp)
    }
}

private[repositories] final case class DoobieSwitchRepository(
  xa: HikariTransactor[Task],
  cfg: AppConfig,
  ndc: NetDataClient.Service,
  seensClient: SeensUtil.Service,
  dns: DNSUtil.Service,
  snmpClient: SNMPUtil.Service
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
      case false => {
        val ip = switch.ipResolveMethod match {
          case "DNS" =>
            for {
              ip <- dns.getIPByHostname(switch.name)
            } yield ip
          case "Direct" => Task.succeed(switch.ip.getOrElse(""))
          case _        => Task.fail(new Exception("unknown IP resolve method"))
        }

        ip.flatMap { ipVal =>
          IO.succeed(
            SwitchResponse(
              switch.name,
              ipVal,
              switch.mac.getOrElse(""),
              buildShortName = switch.buildShortName,
              floorNumber = switch.floorNumber
            )
          )
        }
      }
    }

    val withSNMP = sw.flatMap { s =>
      for {
        maybeSwInfo <- snmpClient
          .getSwitchInfo(s.ip, switch.snmpCommunity)
          .catchAll(e => UIO.succeed(None))
        swInfo = maybeSwInfo.getOrElse(SwitchInfo("", ""))
      } yield s.copy(
        revision = Some(swInfo.revision),
        serial = Some(swInfo.serial)
      )
    }

    val withSeens = withSNMP
      .flatMap { s =>
        for {
          seen <- seensClient.get(s.mac).catchAll(e => UIO.succeed(None))
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
      switch.mac.getOrElse(""),
      switch.revision,
      switch.serial,
      switch.portsNumber,
      switch.buildShortName,
      switch.floorNumber,
      switch.positionTop,
      switch.positionLeft,
      switch.upSwitchName,
      switch.upSwitchMAC,
      switch.upLink
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

  def update(name: String, position: SavePositionRequest): Task[Boolean] = {
    val q = quote {
      Tables.switches
        .filter(sw => sw.name == lift(name))
        .update(
          _.positionTop -> lift(Option(position.top)),
          _.positionLeft -> lift(Option(position.left))
        )
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
