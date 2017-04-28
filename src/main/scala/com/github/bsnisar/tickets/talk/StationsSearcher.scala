package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.telegram._
import com.github.bsnisar.tickets.telegram.TelegramMessages.Update
import com.github.bsnisar.tickets.{Station, Stations}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.matching.Regex


object StationsSearcher {
  val StationsSearchCommands: Regex = "^(/from|/to)\\s.*".r

  def props(stations: Stations, stationId: StationId, telegram: ActorRef): Props =
    Props(classOf[StationsSearcher], stations, stationId, telegram)
}

class StationsSearcher(val stations: Stations,
                       val stationId: StationId,
                       val tg: ActorRef) extends Actor with LazyLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case update: Update =>
      logger.debug(s"try understand ( ${update.text} )")
      val words = update.text.split(" ").toList

      val asyncCall = words match {
        case "/from" :: name :: _ =>
          stations.stationsByName(name)
            .map(mkStationsResponse(_, 'from_stations_found, fromStation = true))
            .recover(doRecover(name))

        case "/to" :: name :: _ =>
          stations.stationsByName(name)
            .map(mkStationsResponse(_, 'to_stations_found, fromStation = false))
            .recover(doRecover(name))

        case _ =>
          Future.successful(MsgCommandFailed('cmd_failed, update.text))
      }

      asyncCall
        .map(update.mkReply)
        .pipeTo(self)

    case msg: TgResponses.Reply =>
      tg ! msg
  }

  private def doRecover(cmd: String): PartialFunction[Throwable, Msg] = {
    case ex =>
      logger.error(s"exception from call", ex)
      MsgCommandFailed('cmd_failed, cmd)
  }

  private def mkStationsResponse(hits: Iterable[Station],
                                 templateId: Symbol,
                                 fromStation: Boolean): Msg = hits match {
    case coll if coll.isEmpty =>
      MsgSimple('stations_not_found)
    case coll =>
      val preparedStations = coll.map { station =>
        val encodedId = stationId.encode(id = station.id, fromStation)
        station.copy(id = encodedId)
      }

      MsgFoundStations(
        id = templateId,
        stations = preparedStations
      )
  }
}
