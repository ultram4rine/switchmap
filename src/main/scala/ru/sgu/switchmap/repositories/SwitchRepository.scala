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
  ] with NetDataClient, SwitchRepository] =
    ZLayer.fromServices[
      DBTransactor.Resource,
      AppConfig,
      NetDataClient.Service,
      SwitchRepository.Service
    ] { (resource, cfg, client) =>
      DoobieSwitchRepository(resource.xa, cfg, client)
    }
}

private[repositories] final case class DoobieSwitchRepository(
  xa: HikariTransactor[Task],
  cfg: AppConfig,
  ndc: NetDataClient.Service
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
    val sw: IO[Status, SwitchResponse] = switch.retrieveFromNetData match {
      case true => {
        val sw = for {
          resp <- ndc.getMatchingHost(
            GetMatchingHostRequest(
              Some(Match(Match.Match.HostName(switch.name)))
            )
          )
          sw = resp.host.head
        } yield SwitchResponse(
          sw.name,
          sw.ipv4Address.mkString("."),
          sw.macAddressString,
          buildShortName = switch.buildShortName,
          floorNumber = switch.floorNumber
        )
        sw
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

    sw.flatMap { s =>
      {
        val q = quote {
          Tables.switches.insert(lift(s))
        }

        Tables.ctx
          .run(q)
          .transact(xa)
          .foldM(err => Task.fail(err), _ => Task.succeed(true))
      }
    }.mapError(s => new Exception(s.toString()))
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
